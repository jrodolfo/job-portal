import { screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import Navbar from './Navbar';
import { renderWithProviders } from '../test/test-utils';

const mockNavigate = vi.fn();

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

describe('Navbar', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('should render username from store', () => {
        renderWithProviders(<Navbar />, {
            preloadedState: {
                user: {
                    username: 'alice',
                    role: 'ROLE_APPLICANT'
                }
            }
        });

        expect(screen.getByText('Welcome, alice')).toBeInTheDocument();
    });

    it('should clear storage, reset user, and navigate on logout', async () => {
        localStorage.setItem('token', 'jwt-123');

        const { store } = renderWithProviders(<Navbar />, {
            preloadedState: {
                user: {
                    username: 'alice',
                    role: 'ROLE_APPLICANT'
                }
            }
        });

        const user = userEvent.setup();
        await user.click(screen.getByRole('button', { name: 'Logout' }));

        expect(localStorage.getItem('token')).toBeNull();
        await waitFor(() => {
            expect(store.getState().user).toEqual({
                username: '',
                role: ''
            });
        });
        expect(mockNavigate).toHaveBeenCalledWith('/');
    });
});
