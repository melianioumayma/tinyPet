export default function PetitionList({ setCurrentProfil, pagePet }) {
	const nbElt = 1;
	const [page, setPage] = React.useState(0);
	const [petitions, setPetitions] = React.useState([]);
	const e = React.createElement;
  
	React.useEffect(() => {
	  fetch("URL_POUR_RECUPERER_PETITIONS")
		.then((res) => {
		  if (res.status !== 200) {
			return;
		  }
		  return res.json();
		})
		.then((data) => {
		  if (data) {
			// Modifier le traitement des données selon la structure de vos données de pétition
			// Par exemple, si les données ont un format différent, vous devrez ajuster cette partie du code.
			setPetitions(data.petitions); // we need wahd tableau d'objets de pétitions
		  } else {
			setPetitions([]);
		  }
		});
	}, []);
  
	return (
	  e("div", { className: "container mx-auto" },
		e("div", { className: "container mx-auto my-10 sm:px-20 flex justify-center gap-2" },
		  petitions.length > 0 ?
			petitions.map((petition, i) => {
			  return (
				i >= page * nbElt && i < (page + 1) * nbElt ?
				  e("div", { className: "rounded overflow-hidden border w-full lg:w-6/12 md:w-6/12 bg-white mx-3 md:mx-0 lg:mx-0", key: petition.id },
					e("div", { className: "w-full flex justify-between p-3" },
					  e("div", { className: "flex" },
						e("div", {
						  className: "rounded-full h-8 w-8 bg-gray-500 flex items-center justify-center overflow-hidden cursor-pointer"
						}, ),
						e("span", {
						  className: "pt-1 ml-2 font-bold text-sm cursor-pointer",
						}, petition.date)
					  )
					),
					e("div", {
					  className: "px-3 pb-2"
					}, e("div", {
					  className: "pt-2"
					}, e("i", {
					  className: "far fa-heart cursor-pointer"
					}), e("div", {
					  className: "mb-2 text-sm"
					}, e("span", {
					  className: "font-medium mr-2"
					}, "Title"), petition.title), e("div", {
					  className: "mb-2 text-sm"
					}, e("span", {
					  className: "font-medium mr-2"
					}, "Description"), petition.description)))
				  ) :
				  null
			  );
			}) :
			e("p", null, "Aucune pétition disponible...")
		),
		petitions.length > 0 && e("div", { className: "flex w-full justify-center gap-2 mb-12" },
		  e("button", { className: "gg-arrow-left", onClick: () => setPage(page - 1), disabled: page === 0 }, null),
		  e("label", null, parseInt(page + 1) + "/" + petitions.length),
		  e("button", { className: "gg-arrow-right", onClick: () => setPage(page + 1), disabled: (page + 1) * nbElt >= petitions.length }, null)
		)
	  )
	);
  }
  