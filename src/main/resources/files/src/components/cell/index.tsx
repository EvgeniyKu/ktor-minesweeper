import style from "./style.module.css";

import type { cellType } from "../../api/typeSocket";

export default function Cell(props: cellType) {
	const { row, column, isOpened, isFlagged, countOfBombs } = props;
	const classCell = [style.cell];
	isOpened && classCell.push(style.open);
	isFlagged && classCell.push(style.flag);
	countOfBombs === 9 && classCell.push(style.bomb);
	return (
		<div
			id="cell"
			className={classCell.join(" ")}
			data-row={row}
			data-column={column}
		>
			{countOfBombs && countOfBombs !== 9 ? countOfBombs : ""}
		</div>
	);
}
