import React from 'react';
import { render } from '@testing-library/react';
import { configureStore } from '@reduxjs/toolkit';
import { Provider } from 'react-redux';
import { MemoryRouter } from 'react-router-dom';
import userReducer from '../store/userReducer';

export function createTestStore(preloadedState) {
    return configureStore({
        reducer: {
            user: userReducer
        },
        preloadedState
    });
}

export function renderWithProviders(
    ui,
    {
        preloadedState = { user: { username: '', role: '' } },
        store = createTestStore(preloadedState),
        route = '/'
    } = {}
) {
    const Wrapper = ({ children }) => (
        <Provider store={store}>
            <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>
        </Provider>
    );

    return {
        store,
        ...render(ui, { wrapper: Wrapper })
    };
}
