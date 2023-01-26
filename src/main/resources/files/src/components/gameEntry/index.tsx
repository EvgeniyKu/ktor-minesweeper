import { Input, Button, Typography } from "antd";

import style from "./style.module.css";

const { Text } = Typography;

export default function GameEntry() {
	return (
		<div className={style.container}>
			<Text>Enter name room:</Text>
			<Input type="text" placeholder="Room name" />
			<Button>Game entry</Button>
		</div>
	);
}
