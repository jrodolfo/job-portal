import { setUserDetails } from './userActions';

describe('userActions', () => {
    it('should dispatch SET_USER_DETAILS with provided payload', () => {
        const dispatch = vi.fn();
        const user = {
            username: 'alice',
            role: 'ROLE_ADMIN'
        };

        setUserDetails(dispatch)(user);

        expect(dispatch).toHaveBeenCalledWith({
            type: 'SET_USER_DETAILS',
            payload: user
        });
    });
});
