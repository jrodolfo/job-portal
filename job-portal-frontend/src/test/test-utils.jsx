import React from 'react';
import {render} from '@testing-library/react';
import {configureStore} from '@reduxjs/toolkit';
import {Provider} from 'react-redux';
import {MemoryRouter} from 'react-router-dom';
import userReducer from '../store/userReducer';

/**
 * Creates a Redux store for testing purposes with preloaded state.
 *
 * @param {Object} [preloadedState] - Initial state for the store.
 * @returns {Object} A configured Redux store.
 */
export function createTestStore(preloadedState) {
    return configureStore({
        reducer: {
            user: userReducer
        },
        preloadedState
    });
}

/**
 * Renders a React component with Redux and React Router providers.
 * Useful for integration tests that depend on the store or routing.
 *
 * @param {React.ReactElement} ui - The component to render.
 * @param {Object} [options] - Configuration options for rendering.
 * @param {Object} [options.preloadedState] - Initial state for the Redux store.
 * @param {Object} [options.store] - A custom store instance.
 * @param {string} [options.route='/'] - The initial route for the MemoryRouter.
 * @returns {Object} An object containing the store and all standard RTL render results.
 */
export function renderWithProviders(
    ui,
    {
        preloadedState = {user: {username: '', role: ''}},
        store = createTestStore(preloadedState),
        route = '/'
    } = {}
) {
    const Wrapper = ({children}) => (
        <Provider store={store}>
            <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>
        </Provider>
    );

    return {
        store,
        ...render(ui, {wrapper: Wrapper})
    };
}
