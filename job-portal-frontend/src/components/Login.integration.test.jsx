import {screen, waitFor} from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import {http, HttpResponse} from 'msw';
import Login from './Login';
import {renderWithProviders} from '../test/test-utils';
import {server} from '../test/mswServer';

const mockNavigate = vi.fn();
const api = 'http://localhost:8080';

vi.mock('react-router-dom', async () => {
    const actual = await vi.importActual('react-router-dom');
    return {
        ...actual,
        useNavigate: () => mockNavigate
    };
});

describe('Login integration', () => {
    beforeEach(() => {
        vi.clearAllMocks();
        localStorage.clear();
    });

    it('logs in through network handlers and navigates to the applicant dashboard', async () => {
        server.use(
            http.post(`${api}/api/auth/login`, ({request}) => {
                const authorization = request.headers.get('authorization');
                if (!authorization?.startsWith('Basic ')) {
                    return HttpResponse.json({message: 'Missing credentials'}, {status: 401});
                }

                return HttpResponse.json({token: 'jwt-123'});
            }),
            http.get(`${api}/api/auth/details`, ({request}) => {
                if (request.headers.get('authorization') !== 'Bearer jwt-123') {
                    return HttpResponse.json({message: 'Forbidden'}, {status: 403});
                }

                return HttpResponse.json({
                    roles: ['ROLE_APPLICANT'],
                    username: 'alice@example.com'
                });
            })
        );

        const {store} = renderWithProviders(<Login/>);
        const user = userEvent.setup();

        await user.type(screen.getByLabelText('Email'), 'alice@example.com');
        await user.type(screen.getByLabelText('Password'), 'secret');
        await user.click(screen.getByRole('button', {name: 'Login'}));

        await waitFor(() => expect(mockNavigate).toHaveBeenCalledWith('/applicant-dashboard'));
        expect(localStorage.getItem('token')).toBe('jwt-123');
        expect(store.getState().user).toEqual({
            username: 'alice@example.com',
            role: 'ROLE_APPLICANT'
        });
    });

    it('shows an inline error when the login request fails at the network layer', async () => {
        server.use(
            http.post(`${api}/api/auth/login`, () => HttpResponse.json({message: 'Internal Server Error'}, {status: 500}))
        );

        renderWithProviders(<Login/>);
        const user = userEvent.setup();

        await user.type(screen.getByLabelText('Email'), 'alice@example.com');
        await user.type(screen.getByLabelText('Password'), 'secret');
        await user.click(screen.getByRole('button', {name: 'Login'}));

        expect(await screen.findByRole('alert')).toHaveTextContent('Invalid email or password.');
        expect(mockNavigate).not.toHaveBeenCalled();
    });
});
