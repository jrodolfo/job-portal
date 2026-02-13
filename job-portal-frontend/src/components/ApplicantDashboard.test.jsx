import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axios from 'axios';
import ApplicantDashboard from './ApplicantDashboard';
import { renderWithProviders } from '../test/test-utils';

vi.mock('axios');
vi.mock('./Navbar', () => ({
    default: () => <div data-testid="navbar-mock">Navbar</div>
}));

describe('ApplicantDashboard', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('should fetch and render jobs on mount', async () => {
        axios.get.mockResolvedValueOnce({
            data: [
                {
                    id: 1,
                    title: 'Java Developer',
                    description: 'Build APIs',
                    company: 'ACME',
                    postedDate: '2026-01-01'
                }
            ]
        });

        renderWithProviders(<ApplicantDashboard />);

        await waitFor(() => expect(axios.get).toHaveBeenCalledWith('http://localhost:8080/api/jobs'));
        expect(screen.getByText('Title: Java Developer')).toBeInTheDocument();
        expect(screen.getByText('Company: ACME')).toBeInTheDocument();
    });

    it('should send apply request with bearer token', async () => {
        const alertSpy = vi.fn();
        vi.stubGlobal('alert', alertSpy);
        localStorage.setItem('token', 'jwt-123');

        axios.get.mockResolvedValueOnce({
            data: [
                {
                    id: 1,
                    title: 'Java Developer',
                    description: 'Build APIs',
                    company: 'ACME',
                    postedDate: '2026-01-01'
                }
            ]
        });
        axios.post.mockResolvedValueOnce({ data: {} });

        renderWithProviders(<ApplicantDashboard />);
        const user = userEvent.setup();

        await waitFor(() => expect(screen.getByRole('button', { name: 'Apply' })).toBeInTheDocument());
        await user.click(screen.getByRole('button', { name: 'Apply' }));

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
        expect(alertSpy).toHaveBeenCalledWith('Application Success!!!');
    });
});
