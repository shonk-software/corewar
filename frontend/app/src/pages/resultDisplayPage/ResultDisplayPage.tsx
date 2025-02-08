import { Button } from "@/components/ui/button";
import { LobbyStatus } from "@/domain/LobbyStatus";
import {
	useDispatchLobby,
	useLobby,
} from "@/services/lobbyContext/LobbyContextHelpers";
import { getLobbyStatusV1WithoutVisuData } from "@/services/rest/LobbyRest";
import { useUser } from "@/services/userContext/UserContextHelpers";
import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

export default function ResultDisplayPageV2() {
	const [lobbyStatus, setLobbyStatus] = useState<LobbyStatus>();
	const navigate = useNavigate();
	const lobby = useLobby();
	const user = useUser();
	const lobbyDispatch = useDispatchLobby();

	useEffect(() => {
		if (lobby) {
			getLobbyStatusV1WithoutVisuData(lobby.id).then((status) =>
				setLobbyStatus(status),
			);
		}
	}, [lobby]);

	function getResultText(): string {
		if (!lobby || !user || !lobbyStatus) {
			return "Error";
		}
		if (lobbyStatus.result.winner === "DRAW") {
			return "It's a draw!";
		} else if (user.name.slice(-1) === lobbyStatus.result.winner) {
			return "You won!";
		} else {
			return "You lost!";
		}
	}

	function getIconForWinner() {
		if (
			!lobbyStatus ||
			lobbyStatus.result.winner === "DRAW" ||
			!lobby ||
			!user
		) {
			return (
				<img
					src={"corewarIcon.svg"}
					alt="draw icon"
					className={"w-164 h-164"}
				/>
			);
		} else if (lobbyStatus.result.winner === "A") {
			return (
				<img
					src={"redPlayerIcon.svg"}
					alt="playerA icon"
					className={"w-164 h-164"}
				/>
			);
		} else {
			return (
				<img
					src={"bluePlayerIcon.svg"}
					alt="playerB icon"
					className={"w-164 h-164"}
				/>
			);
		}
	}

	function getAllPlayerVSString(): string {
		if (!lobby || !lobbyStatus || lobby.playersJoined.length < 2) {
			return "";
		}
		let str = "Result: " + lobby.playersJoined[0];
		for (let i = 1; i < lobby.playersJoined.length; i++) {
			str += " vs " + lobby.playersJoined[i];
		}
		return str;
	}

	function onPlayAgainClick() {
		if (lobbyDispatch) {
			lobbyDispatch({ type: "leave", lobby: null });
		}
		navigate("/lobby-selection");
	}

	return (
		<div>
			<div className="flex flex-col justify-center items-center h-[100%] w-[100%] gap-10">
				<h2 className="text-3xl font-semibold">{getAllPlayerVSString()}</h2>
				<div className="flex flex-row items-center gap-[110px] justify-center">
					<h2 className="text-7xl font-extrabold mb-[110px]">
						{getResultText()}
					</h2>
					{getIconForWinner()}
				</div>
				<Button onClick={onPlayAgainClick}>Play again</Button>
			</div>
		</div>
	);
}
