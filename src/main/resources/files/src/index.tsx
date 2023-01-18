import React from 'react';
import ReactDOM from 'react-dom/client';
import './style.css';
import App from './components/app/App';
import {BrowserRouter } from "react-router-dom";
import store from "./store";
import {Provider} from "react-redux";

const root = ReactDOM.createRoot(
  document.getElementById('root') as HTMLElement
);
root.render(
	<Provider store={store}>
	<BrowserRouter>
	
  <React.StrictMode>
    <App />
  </React.StrictMode>
	
	</BrowserRouter>
	</Provider>
);
