import userReducer from './userReducer';

describe('userReducer', () => {
    it('should return initial state when action is unknown', () => {
        const state = userReducer(undefined, {type: 'UNKNOWN'});

        expect(state).toEqual({
            username: '',
            displayName: '',
            role: ''
        });
    });

    it('should handle SET_USER_DETAILS', () => {
        const action = {
            type: 'SET_USER_DETAILS',
            payload: {
                username: 'alice',
                displayName: 'Alice Smith',
                role: 'ROLE_APPLICANT'
            }
        };

        const state = userReducer(undefined, action);

        expect(state).toEqual({
            username: 'alice',
            displayName: 'Alice Smith',
            role: 'ROLE_APPLICANT'
        });
    });

    it('should fall back to username when displayName is missing', () => {
        const action = {
            type: 'SET_USER_DETAILS',
            payload: {
                username: 'alice@example.com',
                role: 'ROLE_APPLICANT'
            }
        };

        const state = userReducer(undefined, action);

        expect(state).toEqual({
            username: 'alice@example.com',
            displayName: 'alice@example.com',
            role: 'ROLE_APPLICANT'
        });
    });
});
