import { waitFor } from '@testing-library/react';
import axios from 'axios';
import OAuthLogin from './OAuthLogin';
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

describe('OAuthLogin', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
        window.history.pushState({}, '', '/oauthlogon');
    });

    it('should exchange code, fetch user details, update store and navigate', async () => {
        axios.post.mockResolvedValueOnce({
            data: { token: 'oauth-token' }
        });
        axios.get.mockResolvedValueOnce({
            data: { email: 'oauth.user@example.com' }
        });
        window.history.pushState({}, '', '/oauthlogon?code=abc123');

        const { store } = renderWithProviders(<OAuthLogin />);

        await waitFor(() =>
            expect(axios.post).toHaveBeenCalledWith(
                'http://localhost:8080/api/oauth/exchange-token',
                { code: 'abc123' }
            )
        );

        await waitFor(() =>
            expect(axios.get).toHaveBeenCalledWith(
                'http://localhost:8080/api/oauth/user-details',
                {
                    headers: {
                        Authorization: 'Bearer oauth-token'
                    }
                }
            )
        );

        expect(localStorage.getItem('token')).toBe('oauth-token');
        expect(store.getState().user).toEqual({
            username: 'oauth.user@example.com',
            role: 'ROLE_APPLICANT'
        });
        expect(mockNavigate).toHaveBeenCalledWith('/applicant-dashboard');
    });

    it('should not call exchange endpoint when code is missing', async () => {
        renderWithProviders(<OAuthLogin />);

        await waitFor(() => expect(axios.post).not.toHaveBeenCalled());
    });
});
