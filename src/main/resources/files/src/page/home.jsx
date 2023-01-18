import CreateGame from "../components/createGame";
import CreateGameCustom from "../components/createGameCustom";
import GameEntry from "../components/gameEntry";
import { Col, Row, Tabs } from "antd";

export default function Home(props) {
	const itemsTabs = [
		{ label: `Create game`, children: <CreateGame /> },
		{ label: `Create custom game`, children: <CreateGameCustom /> },
		{
			label: `Game Entry`,
			children: <GameEntry />,
		},
	];
	return (
		<Row align="middle" justify="center" style={{ height: "100vh" }}>
			<Col>
				<Tabs
					defaultActiveKey="0"
					tabPosition="top"
					animated={true}
					style={{
						minWidth: "450px",
						width: "40vw",
						height: "50vh",
					}}
					centered={true}
					items={itemsTabs.map((item, i) => {
						item.key = i;
						return item;
					})}
				/>
			</Col>
		</Row>
	);
}
