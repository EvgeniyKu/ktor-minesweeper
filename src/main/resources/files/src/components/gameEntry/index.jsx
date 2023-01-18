import { Input, Button, Typography } from "antd";
import { useNavigate } from "react-router-dom";
import style from "./style.module.css";
const { Text } = Typography;
export default function GameEntry(props) {
	const navigate = useNavigate();
	return (
		<div className={style.container}>
			<Text>Enter name room:</Text>
			<Input type="text" placeholder="Room name" />
			<Button>Game entry</Button>
		</div>
	);
}
