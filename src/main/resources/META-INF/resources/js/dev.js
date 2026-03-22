(function () {
	const protocol = location.protocol === "https:" ? "wss" : "ws";
	const ws = new WebSocket(protocol + "://" + location.host + "/dev-reload");

	ws.onmessage = (event) => {
		if (event.data === "reload") {
			location.reload();
		}
	};
})();

