import {screen, waitFor, within} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {http, HttpResponse} from 'msw';
import ApplicantDashboard from './ApplicantDashboard';
import {renderWithProviders} from '../test/test-utils';
import {server} from '../test/mswServer';

vi.mock('./Navbar', () => ({
    default: () => <div data-testid="navbar-mock">Navbar</div>
}));

const api = 'http://localhost:8080';
const byTextContent = (text) => (_, element) => element?.textContent === text;

describe('ApplicantDashboard integration', () => {
    beforeEach(() => {
        localStorage.clear();
        localStorage.setItem('token', 'jwt-applicant');
    });

    it('loads open jobs and the current applicant applications from real network handlers', async () => {
        server.use(
            http.get(`${api}/api/jobs`, () => HttpResponse.json([
                {
                    id: 1,
                    title: 'Platform Engineer',
                    description: 'Own internal tooling',
                    company: 'OpenAI',
                    postedDate: '2026-05-26'
                },
                {
                    id: 2,
                    title: 'Frontend Engineer',
                    description: 'Build polished UI experiences',
                    company: 'Google',
                    postedDate: '2026-05-25'
                }
            ])),
            http.get(`${api}/api/applications`, () => HttpResponse.json([
                {
                    id: 50,
                    status: 'APPLIED',
                    createdAt: '2026-05-26T11:00:00Z',
                    updatedAt: '2026-05-26T11:00:00Z',
                    job: {id: 1}
                }
            ]))
        );

        renderWithProviders(<ApplicantDashboard/>);

        await waitFor(() => expect(screen.getByText(byTextContent('Platform Engineer'))).toBeInTheDocument());
        expect(screen.getByText(byTextContent('Frontend Engineer'))).toBeInTheDocument();

        const appliedJobCard = screen.getByText(byTextContent('Platform Engineer')).closest('.job-card');
        expect(appliedJobCard).toHaveClass('application-card-applied');
        expect(within(appliedJobCard).getByText(byTextContent('Application Status: Applied'))).toBeInTheDocument();
        expect(within(appliedJobCard).getByText(/Applied On:/)).toBeInTheDocument();
        expect(within(appliedJobCard).queryByText(/Last Updated:/)).not.toBeInTheDocument();
        expect(within(appliedJobCard).getByText('Your application has been submitted and is waiting for review.')).toBeInTheDocument();
        expect(within(appliedJobCard).getByRole('button', {name: 'Withdraw'})).toBeInTheDocument();

        const unappliedJobCard = screen.getByText(byTextContent('Frontend Engineer')).closest('.job-card');
        expect(unappliedJobCard).toHaveClass('application-card-not-applied');
        expect(within(unappliedJobCard).getByText(byTextContent('Application Status: Not applied'))).toBeInTheDocument();
        expect(within(unappliedJobCard).getByText('You have not applied to this job yet.')).toBeInTheDocument();
        expect(within(unappliedJobCard).getByRole('button', {name: 'Apply'})).toBeInTheDocument();
    });

    it('preserves timestamps when a withdraw response returns a partial payload', async () => {
        server.use(
            http.get(`${api}/api/jobs`, () => HttpResponse.json([
                {
                    id: 1,
                    title: 'Platform Engineer',
                    description: 'Own internal tooling',
                    company: 'OpenAI',
                    postedDate: '2026-05-26'
                }
            ])),
            http.get(`${api}/api/applications`, () => HttpResponse.json([
                {
                    id: 50,
                    status: 'APPLIED',
                    createdAt: '2026-05-26T11:00:00Z',
                    updatedAt: '2026-05-26T11:00:00Z',
                    job: {id: 1}
                }
            ])),
            http.put(`${api}/api/applications/50`, () => HttpResponse.json({
                id: 50,
                status: 'WITHDRAWN',
                updatedAt: '2026-05-26T12:30:00Z',
                job: {id: 1}
            }))
        );

        renderWithProviders(<ApplicantDashboard/>);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole('button', {name: 'Withdraw'})).toBeInTheDocument());

        const jobCard = screen.getByText(byTextContent('Platform Engineer')).closest('.job-card');
        expect(within(jobCard).getByText(/Applied On:/)).toBeInTheDocument();
        expect(within(jobCard).queryByText(/Last Updated:/)).not.toBeInTheDocument();

        await user.click(within(jobCard).getByRole('button', {name: 'Withdraw'}));

        expect(await screen.findByText('Application withdrawn successfully.')).toBeInTheDocument();
        expect(jobCard).toHaveClass('application-card-withdrawn');
        expect(within(jobCard).getByText(byTextContent('Application Status: Withdrawn'))).toBeInTheDocument();
        expect(within(jobCard).getByText(/Applied On:/)).toBeInTheDocument();
        expect(within(jobCard).getByText(/Last Updated:/)).toBeInTheDocument();
        expect(within(jobCard).getByText('You withdrew this application and can apply again.')).toBeInTheDocument();
        expect(within(jobCard).getByRole('button', {name: 'Apply Again'})).toBeInTheDocument();
    });

    it('preserves timestamps when a reapply response returns a partial payload', async () => {
        server.use(
            http.get(`${api}/api/jobs`, () => HttpResponse.json([
                {
                    id: 1,
                    title: 'Platform Engineer',
                    description: 'Own internal tooling',
                    company: 'OpenAI',
                    postedDate: '2026-05-26'
                }
            ])),
            http.get(`${api}/api/applications`, () => HttpResponse.json([
                {
                    id: 50,
                    status: 'WITHDRAWN',
                    createdAt: '2026-05-26T11:00:00Z',
                    updatedAt: '2026-05-26T12:30:00Z',
                    job: {id: 1}
                }
            ])),
            http.post(`${api}/api/applications/1`, () => HttpResponse.json({
                id: 50,
                status: 'APPLIED',
                updatedAt: '2026-05-26T13:45:00Z',
                job: {id: 1}
            }))
        );

        renderWithProviders(<ApplicantDashboard/>);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole('button', {name: 'Apply Again'})).toBeInTheDocument());

        const jobCard = screen.getByText(byTextContent('Platform Engineer')).closest('.job-card');
        expect(within(jobCard).getByText(byTextContent('Application Status: Withdrawn'))).toBeInTheDocument();
        expect(within(jobCard).getByText(/Applied On:/)).toBeInTheDocument();
        expect(within(jobCard).getByText(/Last Updated:/)).toBeInTheDocument();

        await user.click(within(jobCard).getByRole('button', {name: 'Apply Again'}));

        expect(await screen.findByText('Application submitted successfully.')).toBeInTheDocument();
        expect(within(jobCard).getByText(byTextContent('Application Status: Applied'))).toBeInTheDocument();
        expect(within(jobCard).getByText(/Applied On:/)).toBeInTheDocument();
        expect(within(jobCard).getByText(/Last Updated:/)).toBeInTheDocument();
        expect(within(jobCard).getByText('Your application has been submitted and is waiting for review.')).toBeInTheDocument();
        expect(within(jobCard).getByRole('button', {name: 'Withdraw'})).toBeInTheDocument();
    });
});
