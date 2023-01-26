import React, { FC } from "react";
import Logic from "./logic";
import Ui from "./ui";

export type prop = {};

export default function (props: prop): React.ReactElement {
	return Logic({ Ui, ...props });
}
