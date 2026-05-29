// src/store/userSelectors.js

/**
 * Selector to retrieve the user object from the Redux state.
 *
 * @param {Object} state - The global Redux state.
 * @returns {Object} The user state slice.
 */
export const selectUser = (state) => state.user;