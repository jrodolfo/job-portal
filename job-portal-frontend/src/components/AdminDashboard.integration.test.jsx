import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {http, HttpResponse} from 'msw';
import AdminDashboard from './AdminDashboard';
import {renderWithProviders} from '../test/test-utils';
import {server} from '../test/mswServer';

vi.mock('./Navbar', () => ({
    default: () => <div data-testid="navbar-mock">Navbar</div>
}));

const api = 'http://localhost:8080';
const byTextContent = (text) => (_, element) => element?.textContent === text;

describe('AdminDashboard integration', () => {
    beforeEach(() => {
        localStorage.clear();
        localStorage.setItem('token', 'jwt-admin');
    });

    it('loads admin jobs and applications from real network handlers', async () => {
        server.use(
            http.get(`${api}/api/jobs/admin`, () => HttpResponse.json([
                {
                    id: 1,
                    title: 'Platform Engineer',
                    description: 'Own internal tooling',
                    company: 'OpenAI',
                    createdAt: '2026-05-26T10:00:00Z',
                    status: 'OPEN'
                }
            ])),
            http.get(`${api}/api/applications`, () => HttpResponse.json([
                {
                    id: 10,
                    status: 'APPLIED',
                    createdAt: '2026-05-26T11:00:00Z',
                    updatedAt: '2026-05-26T11:00:00Z',
                    user: {name: 'Maya Patel'},
                    job: {id: 1, title: 'Platform Engineer'}
                }
            ])),
            http.get(`${api}/api/users/admin`, () => HttpResponse.json([
                {
                    id: 1,
                    name: 'admin',
                    email: 'admin@local.test',
                    role: 'ADMIN',
                    enabled: true
                },
                {
                    id: 2,
                    name: 'user',
                    email: 'user@local.test',
                    role: 'APPLICANT',
                    enabled: true
                }
            ]))
        );

        renderWithProviders(<AdminDashboard/>);

        await waitFor(() => expect(screen.getByText(byTextContent('Platform Engineer'))).toBeInTheDocument());
        expect(screen.getByRole('tab', {name: /^Jobs \(\d+\)$/})).toHaveAttribute('aria-selected', 'true');
        expect(within(screen.getByText('Total Jobs').closest('.admin-overview-card')).getByText('1')).toBeInTheDocument();
        const applicationsOverviewCard = screen
            .getAllByText('Applications')
            .find((element) => element.classList.contains('admin-overview-label'))
            ?.closest('.admin-overview-card');
        expect(applicationsOverviewCard).not.toBeNull();
        expect(within(applicationsOverviewCard).getByText('1')).toBeInTheDocument();
        expect(screen.getByText('Open: 1 | Closed: 0')).toBeInTheDocument();
        expect(screen.getByText('Applied: 1 | Reviewing: 0 | Accepted: 0 | Rejected: 0 | Withdrawn: 0')).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Applications: 1'))).toBeInTheDocument();
        expect(screen.getByRole('tab', {name: /^Users \(\d+\)$/})).toBeInTheDocument();
    });

    it('preserves timestamps when the application status update response is partial', async () => {
        server.use(
            http.get(`${api}/api/jobs/admin`, () => HttpResponse.json([
                {
                    id: 1,
                    title: 'Platform Engineer',
                    description: 'Own internal tooling',
                    company: 'OpenAI',
                    createdAt: '2026-05-26T10:00:00Z',
                    status: 'OPEN'
                }
            ])),
            http.get(`${api}/api/applications`, () => HttpResponse.json([
                {
                    id: 10,
                    status: 'APPLIED',
                    createdAt: '2026-05-26T11:00:00Z',
                    updatedAt: '2026-05-26T11:00:00Z',
                    user: {name: 'Maya Patel'},
                    job: {id: 1, title: 'Platform Engineer'}
                }
            ])),
            http.get(`${api}/api/users/admin`, () => HttpResponse.json([
                {
                    id: 1,
                    name: 'admin',
                    email: 'admin@local.test',
                    role: 'ADMIN',
                    enabled: true
                }
            ])),
            http.put(`${api}/api/applications/10`, async () => HttpResponse.json({
                id: 10,
                status: 'REVIEWING',
                updatedAt: '2026-05-26T12:30:00Z',
                user: {name: 'Maya Patel'},
                job: {id: 1, title: 'Platform Engineer'}
            }))
        );

        renderWithProviders(<AdminDashboard/>);
        const user = userEvent.setup();

        await user.click(screen.getByRole('tab', {name: /^Applications \(\d+\)$/}));
        await waitFor(() => expect(screen.getByLabelText('Status')).toBeInTheDocument());

        const applicationCard = screen.getByText(byTextContent('Applicant: Maya Patel')).closest('.application-card');
        expect(within(applicationCard).getByText(/Applied On:/)).toBeInTheDocument();
        expect(within(applicationCard).queryByText(/Last Updated:/)).not.toBeInTheDocument();

        await user.selectOptions(screen.getByLabelText('Status'), 'REVIEWING');
        await user.click(screen.getByRole('button', {name: 'Save'}));

        expect(await screen.findByText('Application status updated successfully.')).toBeInTheDocument();
        const updatedApplicationCard = screen.getByText(byTextContent('Applicant: Maya Patel')).closest('.application-card');
        expect(within(updatedApplicationCard).getByText(byTextContent('Current Status: Reviewing'))).toBeInTheDocument();
        expect(within(updatedApplicationCard).getByText(/Applied On:/)).toBeInTheDocument();
        expect(within(updatedApplicationCard).getByText(/Last Updated:/)).toBeInTheDocument();
    });

    it('creates applicant users through the admin users network flow', async () => {
        const createdUser = {
            id: 20,
            name: 'Sofia Ribeiro',
            email: 'sofia.ribeiro@example.com',
            role: 'APPLICANT',
            enabled: true
        };

        server.use(
            http.get(`${api}/api/jobs/admin`, () => HttpResponse.json([])),
            http.get(`${api}/api/applications`, () => HttpResponse.json([])),
            http.get(`${api}/api/users/admin`, () => HttpResponse.json([
                {
                    id: 1,
                    name: 'admin',
                    email: 'admin@local.test',
                    role: 'ADMIN',
                    enabled: true
                }
            ])),
            http.post(`${api}/api/users/admin/applicants`, async ({request}) => {
                const body = await request.json();

                if (
                    body.name !== 'Sofia Ribeiro' ||
                    body.email !== 'sofia.ribeiro@example.com' ||
                    body.password !== 'applicant123'
                ) {
                    return HttpResponse.json({message: 'Unexpected applicant payload'}, {status: 400});
                }

                return HttpResponse.json(createdUser, {status: 201});
            })
        );

        renderWithProviders(<AdminDashboard/>);
        const user = userEvent.setup();

        await user.click(await screen.findByRole('tab', {name: /^Users \(\d+\)$/}));
        await user.type(screen.getByLabelText('Name'), 'Sofia Ribeiro');
        await user.type(screen.getByLabelText('Email'), 'sofia.ribeiro@example.com');
        await user.type(screen.getByLabelText('Password'), 'applicant123');
        await user.click(screen.getByRole('button', {name: 'Create Applicant'}));

        expect(await screen.findByText('Applicant user created successfully.')).toBeInTheDocument();
        expect(screen.getByRole('tab', {name: 'Users (2)'})).toBeInTheDocument();
        const userCard = screen.getByText('Sofia Ribeiro').closest('.user-card');
        expect(within(userCard).getByText('Email:')).toBeInTheDocument();
        expect(within(userCard).getByText(/sofia\.ribeiro@example\.com/)).toBeInTheDocument();
        expect(within(userCard).getByText('Status:')).toBeInTheDocument();
        expect(within(userCard).getByText(/Enabled/)).toBeInTheDocument();
    });
});
