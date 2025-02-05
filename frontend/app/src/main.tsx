import { ThemeProvider } from "@/components/ui/ThemeProvider.tsx";
import BasePage from "@/pages/basePage/BasePage.tsx";
import ErrorPage from "@/pages/ErrorPage.tsx";
import LandingPage from "@/pages/landingPage/LandingPage.tsx";
import PlayerSelection from "@/pages/playerSelection/PlayerSelection.tsx";
import WaitingForOpponent from "@/pages/waitingForOpponent/WaitingForOpponent.tsx";
import WaitingForResultPage from "@/pages/waitingForResult/WaitingForResultPage.tsx";
import { UserProvider } from "@/services/userContext/UserContext.tsx";
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { createBrowserRouter, RouterProvider } from "react-router-dom";
import { Toaster } from "./components/ui/toaster.tsx";
import "./index.css";
import "./monacoConfig";
import LobbySelectionPage from "./pages/lobbySelectionPage/LobbySelectionPage.tsx";
import PlayerCodingPage from "./pages/playerCodeInput/PlayerCodingPage.tsx";
import ResultDisplayPage from "./pages/resultDisplayPage/ResultDisplayPage.tsx";
import { LobbyProvider } from "./services/lobbyContext/LobbyContext.tsx";
import CanvasGameVisuPage from "@/pages/canvasGameVisuPage/CanvasGameVisuPage.tsx";

const router = createBrowserRouter([
	{
		path: "/",
		element: <BasePage />,
		errorElement: <ErrorPage />,
		children: [
			{
				index: true,
				element: <LandingPage enableThreeJs={true} />,
			},
			{
				path: "waiting-for-result",
				element: <WaitingForResultPage />,
			},
			{
				path: "waiting-for-opponent",
				element: <WaitingForOpponent />,
			},
			{
				path: "player-coding",
				element: <PlayerCodingPage />,
			},
			{
				path: "/player-selection",
				element: <PlayerSelection />,
			},
			{
				path: "/result-display",
				element: <ResultDisplayPage />,
			},
			{
				path: "/lobby-selection",
				element: <LobbySelectionPage />,
			},
		],
	},
	{
		path: "/visu",
		element: <CanvasGameVisuPage></CanvasGameVisuPage>,
	},
]);

createRoot(document.getElementById("root")!).render(
	<StrictMode>
		<UserProvider>
			<LobbyProvider>
				<ThemeProvider defaultTheme="dark" storageKey="vite-ui-theme">
					<RouterProvider router={router} />
					<Toaster />
				</ThemeProvider>
			</LobbyProvider>
		</UserProvider>
	</StrictMode>,
);
