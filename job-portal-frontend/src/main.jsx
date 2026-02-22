import {createRoot} from 'react-dom/client'
import App from './App.jsx'

import "bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import "./index.css";
import "./App.css";
import store from './store/store.js';
import {Provider} from 'react-redux';

createRoot(document.getElementById('root')).render(
    <Provider store={store}>
        <App/>
    </Provider>
)
