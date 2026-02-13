import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import axios from 'axios';
import Login from './Login';
import { renderWithProviders } from '../test/test-utils';

const mockNavigate = vi.fn();

vi.mock('axios');
vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

describe('Login', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    afterEach(() => {
        vi.unstubAllGlobals();
    });

    it('should login applicant, save token, set user details and navigate', async () => {
        axios.post.mockResolvedValueOnce({ data: { token: 'jwt-123' } });
        axios.get.mockResolvedValueOnce({
            data: { roles: ['ROLE_APPLICANT'], username: 'alice' }
        });

        const { store } = renderWithProviders(<Login />);
        const user = userEvent.setup();

        await user.type(screen.getByPlaceholderText('Username'), 'alice');
        await user.type(screen.getByPlaceholderText('Password'), 'secret');
        await user.click(screen.getByRole('button', { name: 'Login' }));

        await waitFor(() =>
            expect(axios.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/auth/login',
                {},
                expect.objectContaining({
                    headers: expect.objectContaining({
                        Authorization: expect.stringContaining('Basic ')
                    })
                })
            )
        );

        expect(localStorage.getItem('token')).toBe('jwt-123');
        expect(store.getState().user).toEqual({
            username: 'alice',
            role: 'ROLE_APPLICANT'
        });
        expect(mockNavigate).toHaveBeenCalledWith('/applicant-dashboard');
    });

    it('should alert on invalid credentials', async () => {
        const alertSpy = vi.fn();
        vi.stubGlobal('alert', alertSpy);
        axios.post.mockRejectedValueOnce(new Error('unauthorized'));

        renderWithProviders(<Login />);
        const user = userEvent.setup();

        await user.type(screen.getByPlaceholderText('Username'), 'alice');
        await user.type(screen.getByPlaceholderText('Password'), 'bad');
        await user.click(screen.getByRole('button', { name: 'Login' }));

        await waitFor(() => expect(alertSpy).toHaveBeenCalledWith('Invalid credentials'));
    });

    it('should redirect to Google OAuth endpoint when Google button is clicked', async () => {
        renderWithProviders(<Login />);
        const user = userEvent.setup();

        await user.click(screen.getByRole('button', { name: 'Sign in with Google' }));

        expect(window.location.href).toContain('/oauth2/authorization/google');
    });
});
