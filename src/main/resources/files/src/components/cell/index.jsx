import style from "./style.module.css";
export default function Cell(props) {
	const { idRow, idColumn, isOpened, isFlagged, countOfBombs } = props;
	const classCell = [style.cell];
	isOpened && classCell.push(style.open);
	isFlagged && classCell.push(style.flag);
	countOfBombs === 9 && classCell.push(style.bomb);
	return (
		<div
			id="cell"
			className={classCell.join(" ")}
			data-row={idRow}
			data-column={idColumn}
		>
			{countOfBombs && countOfBombs !== 9 ? countOfBombs : ""}
		</div>
	);
}
