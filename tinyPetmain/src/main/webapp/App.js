import PetitionList from "./PetitionList.js";

function App() {
	
	const e = React.createElement
	const loginRef = React.useRef(null);
	const [petitions, setPetitions] = React.useState(true);
	const [user, setUser] = React.useState({});
	
	const [currentProfil, setCurrentProfil] = React.useState("");
	
	let hasSession = Object.keys(user).length != 0
		
	function retrieveSession() {
		fetch("http://localhost:5500/connection")//url app deploiement.
			.then((res) => {
				return res.text();
			}).then((data) => {
				const rep = JSON.parse(data)
				if(rep.session) {
					setUser({id: rep.id})
				}
			});
	}
	
	function handleConnection(token) {
		fetch("http://localhost:5500/connection?token="+token)//url app deploiement.
			.then((res) => {
				return res.text();
			}).then((data) => {
				const rep = JSON.parse(data)
				if(rep.session) {
					setUser({id: rep.id})
				}
			});
	}
	
	function handleCallbackResponse(response) {
		const repPayload = jwt_decode(response.credential);
		handleConnection(response.credential)
	}
	
	function handleSignOut(e) {
		setUser({});
		fetch("http://localhost:5500/disconnection")
			.then((res) => {
				console.log(res);
			})
	}
	
	React.useEffect(() => {
		retrieveSession();
		if(Object.keys(user).length == 0) {
			google.accounts.id.initialize({
				client_id: "909783088248-9rg5s8fdfkn402gkkiugmmbc3pjnm2e5.apps.googleusercontent.com",
				callback: handleCallbackResponse
			});
			google.accounts.id.renderButton(loginRef.current, {theme: "outline", size:"large"});
		}
	}, [loginRef.current]);
  
	return (
		e("div", { className: "w-screen" },
			hasSession && petitionlist && e(PetitionList,{setCurrentProfil, pagePet: setPetitions},null),
			hasSession && (currentProfil != "") && e(Profil, {currentProfil, userId: user.id, handleSignOut, setCurrentProfil}, null),
			!hasSession && e("div", {ref: loginRef}, null),
		)
	);
}
export default App;