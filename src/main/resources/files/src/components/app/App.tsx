import { Route, Routes } from "react-router-dom";
import Game from "../../page/game";
import Home from "../../page/home";
import NotFound from "../../page/notFound";

function App() {
	return (
		<Routes>
			<Route index element={<Home />} />
			<Route path="game/:roomName/:userName" element={<Game />} />
			<Route path="*" element={<NotFound />} />
		</Routes>
	);
}

export default App;
