import "./Header.css";
import { User } from "@/domain/User.ts";
import { useUser } from "@/services/userContext/UserContextHelpers.ts";
import { useLocation } from "react-router-dom";
import { Lobby } from "@/domain/Lobby.ts";
import { useLobby } from "@/services/lobbyContext/LobbyContextHelpers.ts";

function Header() {
	const user: User | null = useUser();
	const location = useLocation();
	const lobby: Lobby | null = useLobby();

	const copyLobbyIdToClipboard = async () => {
		await navigator.clipboard.writeText(lobby ? lobby.id.toString() : "");
	};

	return (
		<div id="headerContainer">
			<div id="headerText">
				<h2 className="text-3xl font-semibold">
					{location.pathname.length !== 1 && "Corewar"}
				</h2>
			</div>
			{lobby !== null && (
				<button
					id="lobbyInfoButton"
					className={"font-semibold"}
					onClick={copyLobbyIdToClipboard}
				>
					<img
						src={"copy.svg"}
						alt={"An icon indicating that this button triggers a copy action"}
						style={{ marginRight: "8px" }}
					/>
					Lobby ID: {lobby?.id}
				</button>
			)}
			<div id="player">
				{user != null && (
					<>
						<svg
							width="36"
							height="36"
							id="playerIcon"
							role="img"
							aria-label="A colored circle"
						>
							<circle cx="18" cy="18" r="18" fill={user?.colorHex || "red"} />
						</svg>
						<small
							className="text-sm font-semibold leading-none"
							id="playerName"
						>
							{user?.name}
						</small>
					</>
				)}
			</div>
		</div>
	);
}

export default Header;
