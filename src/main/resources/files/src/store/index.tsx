import stateGame, {stateGameType} from "./stateGame";
import { configureStore } from "@reduxjs/toolkit";

export type storeType = {
	stateGame:stateGameType 
}

const store = configureStore({
	reducer: {
		stateGame,
	},
});
export default store;
