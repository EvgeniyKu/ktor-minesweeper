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
        <Route path="/" element={<Layout />}>
          <Route index element={<Home />} />
          <Route path="game" element={<Game />} />
          <Route path="*" element={<NotFound />} />
        </Route>
	</Routes>
      
  );
}

export default App;


function Layout() {
  return (
    <div>
      {/* A "layout route" is a good place to put markup you want to
          share across all the pages on your site, like navigation. */}
      <nav>
        <ul>
          <li>
            <Link to="/">Home</Link>
          </li>
          <li>
            <Link to="/game">Game</Link>
          </li>
        </ul>
      </nav>

      <hr />

      {/* An <Outlet> renders whatever child route is currently active,
          so you can think about this <Outlet> as a placeholder for
          the child routes we defined above. */}
      <Outlet />
    </div>
  );
}

