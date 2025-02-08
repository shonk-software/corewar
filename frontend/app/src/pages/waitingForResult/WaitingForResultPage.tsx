import LoadingSpinner from "@/components/loadingSpinner/LoadingSpinner.tsx";
import { RequireUser } from "@/components/requireUser.tsx/RequireUser";
import { GameState } from "@/domain/GameState.ts";
import { BASE_POLLING_INTERVAL_MS } from "@/consts.ts";
import { usePageVisibility } from "@/lib/usePageVisibility.ts";
import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import styles from "./WaitingForResult.module.css";
import { getLobbyStatusV1WithVisuData } from "@/services/rest/LobbyRest.ts";
import { useLobby } from "@/services/lobbyContext/LobbyContextHelpers.ts";

function WaitingForResultPage() {
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

			console.log(status);

			if (status.gameState === GameState.FINISHED) {
				setIsPollingEnabled(false);
				console.log("Game finished. Stopped polling. Rerouting");
				navigate("/visu");
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
			<div id={styles["waitingForResultHeadline"]}>
				<h2 className="text-3xl font-semibold">Waiting for game result...</h2>
			</div>
			<div id={styles["loadingSpinnerContainer"]}>
				<LoadingSpinner />
			</div>
		</RequireUser>
	);
}

export default WaitingForResultPage;
