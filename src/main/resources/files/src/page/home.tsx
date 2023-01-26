import React from "react";
import { Col, Row, Tabs } from "antd";
import CreateGame from "../components/createGame";
import CreateGameCustom from "../components/createGameCustom";
import GameEntry from "../components/gameEntry";

type itemTabType = {
	label: string;
	key: string;
	children: React.ReactNode;
};
type itemsTabsType = Array<itemTabType>;
type propsType = {};
export default function Home(props: propsType) {
	const itemsTabs: itemsTabsType = [
		{ key: "0", label: `Create game`, children: <CreateGame /> },
		{ key: "1", label: `Create custom game`, children: <CreateGameCustom /> },
		{ key: "2", label: `Game Entry`, children: <GameEntry /> },
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
					items={itemsTabs}
				/>
			</Col>
		</Row>
	);
}
