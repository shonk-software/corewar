import LoadingSpinner from "@/components/loadingSpinner/LoadingSpinner.tsx";
import { RequireUser } from "@/components/requireUser.tsx/RequireUser";
import { GameState } from "@/domain/GameState.ts";
import { BASE_POLLING_INTERVAL_MS } from "@/consts.ts";
import { usePageVisibility } from "@/lib/usePageVisibility.ts";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./WaitingForOpponent.module.css";
import { useLobby } from "@/services/lobbyContext/LobbyContextHelpers.ts";
import { getLobbyStatusV1WithVisuData } from "@/services/rest/LobbyRest.ts";

function WaitingForOpponentPage() {
	const isPageVisible = usePageVisibility();
	const timerIdRef = useRef<ReturnType<typeof setInterval> | null>(null);
	const [isPollingEnabled, setIsPollingEnabled] = useState(true);
	const navigate = useNavigate();
	const lobby = useLobby();

	useEffect(() => {
		const pollingCallback = async () => {
			console.debug("Polling game status...");

			if (!lobby) {
				console.error("No lobby!? How the f*ck did you manage to do this???");
				return;
			}
			const status = await getLobbyStatusV1WithVisuData(lobby.id);

			// TODO: Change getLobbyStatusV1 to include status IF WE STILL WANT TO KEEP THIS PART BELOW
			/*			if (status.status >= 500) {
				return;
			}*/

			if (status.gameState != GameState.NOT_STARTED) {
				setIsPollingEnabled(false);
				console.log("Game started. Stopped polling. Rerouting");
				navigate("/waiting-for-result");
			}
		};

		const startPolling = () => {
			timerIdRef.current = setInterval(
				pollingCallback,
				BASE_POLLING_INTERVAL_MS,
			);
		};

		const stopPolling = () => {
			if (timerIdRef.current !== null) {
				clearInterval(timerIdRef.current);
				timerIdRef.current = null;
			}
		};

		if (isPageVisible && isPollingEnabled) {
			startPolling();
		} else {
			stopPolling();
		}

		return () => {
			stopPolling();
		};
	}, [lobby, isPageVisible, isPollingEnabled, navigate]);

	return (
		<RequireUser>
			<div id={styles["waitingForOpponentHeadline"]}>
				<h2 className="text-3xl font-semibold">Waiting for opponent...</h2>
			</div>
			<div id={styles["loadingSpinnerContainer"]}>
				<LoadingSpinner />
			</div>
		</RequireUser>
	);
}

export default WaitingForOpponentPage;
