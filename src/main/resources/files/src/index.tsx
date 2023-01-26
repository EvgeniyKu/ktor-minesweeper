import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { Provider } from "react-redux";

import "./style.css";
import App from "./components/app/App";
import store from "./store";

const root = ReactDOM.createRoot(
	document.getElementById("root") as HTMLDivElement
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
