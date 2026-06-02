import {screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axios from 'axios';
import ApplicantDashboard from './ApplicantDashboard';
import {renderWithProviders} from '../test/test-utils';

vi.mock('axios');
vi.mock('./Navbar', () => ({
    default: () => <div data-testid="navbar-mock">Navbar</div>
}));

const applicationDateTimeFormatter = new Intl.DateTimeFormat(undefined, {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit'
});

const byTextContent = (text) => (_, element) => element?.textContent === text;

describe('ApplicantDashboard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
        localStorage.setItem('token', 'jwt-123');
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('should fetch and render jobs on mount', async () => {
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});

        renderWithProviders(<ApplicantDashboard/>);

        await waitFor(() => expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/jobs'));
        expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/applications', {
            headers: {
                Authorization: 'Bearer jwt-123'
            }
        });
        expect(screen.getByText(byTextContent('Java Developer'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Company: ACME'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Application Status: Not applied'))).toBeInTheDocument();
        expect(screen.getByLabelText('Application status color legend')).toHaveTextContent('Not applied');
        expect(screen.getByLabelText('Application status color legend')).toHaveTextContent('Accepted');
        expect(screen.getByText(byTextContent('Java Developer')).closest('.job-card')).toHaveClass('application-card-not-applied');
    });

    it('should clear the session when application loading returns unauthorized', async () => {
        axios.get
            .mockResolvedValueOnce({data: []})
            .mockRejectedValueOnce({
                response: {
                    status: 401,
                    data: {
                        message: 'Your account is disabled. Please contact an administrator.'
                    }
                }
            });

        const {store} = renderWithProviders(<ApplicantDashboard/>, {
            preloadedState: {
                user: {
                    username: 'alice@example.com',
                    displayName: 'Alice Smith',
                    role: 'ROLE_APPLICANT'
                }
            }
        });

        await waitFor(() => expect(localStorage.getItem('token')).toBeNull());
        expect(store.getState().user).toEqual({
            username: '',
            displayName: '',
            role: ''
        });
    });

    it('should send apply request with bearer token', async () => {
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});
        axios.post.mockResolvedValueOnce({
            data: {
                id: 50,
                status: 'APPLIED',
                job: {
                    id: 1
                }
            }
        });

        renderWithProviders(<ApplicantDashboard/>);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole('button', {name: 'Apply'})).toBeInTheDocument());
        await user.click(screen.getByRole('button', {name: 'Apply'}));

        await waitFor(() =>
            expect(axios.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/applications/1',
                {},
                {
                    headers: {
                        Authorization: 'Bearer jwt-123'
                    }
                }
            )
        );
        expect(await screen.findByText('Application submitted successfully.')).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Application Status: Applied'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Java Developer')).closest('.job-card')).toHaveClass('application-card-applied');
        expect(screen.getByText('Your application has been submitted and is waiting for review.')).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Withdraw'})).toBeInTheDocument();
    });

    it('should show inline message when apply request fails', async () => {
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({data: []});
        axios.post.mockRejectedValueOnce({
            response: {
                status: 500,
                data: {
                    message: 'A server error occurred while processing your request.'
                }
            }
        });

        renderWithProviders(<ApplicantDashboard/>);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole('button', {name: 'Apply'})).toBeInTheDocument());
        await user.click(screen.getByRole('button', {name: 'Apply'}));

        expect(await screen.findByText('A server error occurred while processing your request.')).toBeInTheDocument();
    });

    it('should disable apply and show status when an application already exists', async () => {
        const createdAt = '2026-05-26T14:43:00Z';
        const updatedAt = '2026-05-26T14:43:00Z';
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 50,
                        status: 'APPLIED',
                        createdAt,
                        updatedAt,
                        job: {
                            id: 1
                        }
                    }
                ]
            });

        renderWithProviders(<ApplicantDashboard/>);

        expect(await screen.findByText(byTextContent('Application Status: Applied'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Java Developer')).closest('.job-card')).toHaveClass('application-card-applied');
        expect(screen.getByText(byTextContent(`Applied On: ${applicationDateTimeFormatter.format(new Date(createdAt))}`))).toBeInTheDocument();
        expect(screen.queryByText(/Last Updated:/)).not.toBeInTheDocument();
        expect(screen.getByText('Your application has been submitted and is waiting for review.')).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Withdraw'})).toBeInTheDocument();
        expect(axios.post).not.toHaveBeenCalled();
    });

    it('should withdraw an applied application and allow reapply after reload state changes', async () => {
        const createdAt = '2026-05-26T14:43:00Z';
        const updatedAt = '2026-05-26T15:10:00Z';
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 50,
                        status: 'APPLIED',
                        createdAt,
                        job: {
                            id: 1
                        }
                    }
                ]
            });
        axios.put.mockResolvedValueOnce({
            data: {
                id: 50,
                status: 'WITHDRAWN',
                updatedAt,
                job: {
                    id: 1
                }
            }
        });

        renderWithProviders(<ApplicantDashboard/>);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole('button', {name: 'Withdraw'})).toBeInTheDocument());
        await user.click(screen.getByRole('button', {name: 'Withdraw'}));

        await waitFor(() =>
            expect(axios.put).toHaveBeenCalledWith(
                'http://localhost:8080/api/applications/50',
                null,
                {
                    params: {
                        status: 'WITHDRAWN'
                    },
                    headers: {
                        Authorization: 'Bearer jwt-123'
                    }
                }
            )
        );

        expect(await screen.findByText('Application withdrawn successfully.')).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Application Status: Withdrawn'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent(`Applied On: ${applicationDateTimeFormatter.format(new Date(createdAt))}`))).toBeInTheDocument();
        expect(screen.getByText(byTextContent(`Last Updated: ${applicationDateTimeFormatter.format(new Date(updatedAt))}`))).toBeInTheDocument();
        expect(screen.getByText('You withdrew this application and can apply again.')).toBeInTheDocument();
        expect(screen.getByRole('button', {name: 'Apply Again'})).toBeInTheDocument();
    });

    it('should show backend message when withdraw request fails', async () => {
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 50,
                        status: 'APPLIED',
                        job: {
                            id: 1
                        }
                    }
                ]
            });
        axios.put.mockRejectedValueOnce({
            response: {
                status: 403,
                data: {
                    message: 'Applicants can only set application status to WITHDRAWN'
                }
            }
        });

        renderWithProviders(<ApplicantDashboard/>);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole('button', {name: 'Withdraw'})).toBeInTheDocument());
        await user.click(screen.getByRole('button', {name: 'Withdraw'}));

        expect(await screen.findByText('Applicants can only set application status to WITHDRAWN')).toBeInTheDocument();
    });

    it('should show reviewing helper text for reviewed applications', async () => {
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 50,
                        status: 'REVIEWING',
                        job: {
                            id: 1
                        }
                    }
                ]
            });

        renderWithProviders(<ApplicantDashboard/>);

        expect(await screen.findByText(byTextContent('Application Status: Reviewing'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Java Developer')).closest('.job-card')).toHaveClass('application-card-reviewing');
        expect(screen.getByText('Your application is currently under review.')).toBeInTheDocument();
    });

    it('should show accepted helper text for reviewed applications', async () => {
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 50,
                        status: 'ACCEPTED',
                        job: {
                            id: 1
                        }
                    }
                ]
            });

        renderWithProviders(<ApplicantDashboard/>);

        expect(await screen.findByText(byTextContent('Application Status: Accepted'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Java Developer')).closest('.job-card')).toHaveClass('application-card-accepted');
        expect(screen.getByText('Your application has been accepted.')).toBeInTheDocument();
    });

    it('should show rejected helper text for reviewed applications', async () => {
        axios.get
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 1,
                        title: 'Java Developer',
                        description: 'Build APIs',
                        company: 'ACME',
                        postedDate: '2026-01-01'
                    }
                ]
            })
            .mockResolvedValueOnce({
                data: [
                    {
                        id: 50,
                        status: 'REJECTED',
                        job: {
                            id: 1
                        }
                    }
                ]
            });

        renderWithProviders(<ApplicantDashboard/>);

        expect(await screen.findByText(byTextContent('Application Status: Rejected'))).toBeInTheDocument();
        expect(screen.getByText(byTextContent('Java Developer')).closest('.job-card')).toHaveClass('application-card-rejected');
        expect(screen.getByText('Your application was not selected.')).toBeInTheDocument();
    });
});
