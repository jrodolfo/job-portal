import userReducer from './userReducer';

describe('userReducer', () => {
    it('should return initial state when action is unknown', () => {
        const state = userReducer(undefined, { type: 'UNKNOWN' });

        expect(state).toEqual({
            username: '',
            role: ''
        });
    });

    it('should handle SET_USER_DETAILS', () => {
        const action = {
            type: 'SET_USER_DETAILS',
            payload: {
                username: 'alice',
                role: 'ROLE_APPLICANT'
            }
        };

        const state = userReducer(undefined, action);

        expect(state).toEqual({
            username: 'alice',
            role: 'ROLE_APPLICANT'
        });
    });
});
