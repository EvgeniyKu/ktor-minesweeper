import { useEffect } from "react";
import { useDispatch } from "react-redux";
import { Route, Outlet, Link, Routes } from "react-router-dom";
import Game from './page/game';
import Home from './page/home';
import NotFound from "./page/notFound";
import socket from "./socket";
import { setStateGame } from "./store/stateGame";


function App() {
	const dispatch = useDispatch();
	useEffect(() => {
		const handleMessage = (message:{data:string}) => {
			const data = JSON.parse(message.data);
			switch (data["messageType"]) {
				case "game_state":
					const body = data["body"];
					console.log(`Body response:`, body);
					dispatch(setStateGame(body));
					break;
			}
		};
		socket.addEventListener("message", handleMessage);
		return () => socket.removeEventListener("message", handleMessage);
	}, []);

	return (
		
			<Routes>
          <Route index element={<Home />} />
          <Route path="game" element={<Game />} />
          <Route path="*" element={<NotFound />} />
	</Routes>
      
  );
}

export default App;