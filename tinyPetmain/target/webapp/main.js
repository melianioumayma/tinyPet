
var MyApp = (function (superclass) {
  function MyApp(props) {
    superclass.call(this, props);

    this.state = {
      data: [],
    };
  }

  if ( superclass ) MyApp.__proto__ = superclass;
  MyApp.prototype = Object.create( superclass && superclass.prototype );
  MyApp.prototype.constructor = MyApp;

  MyApp.prototype.componentDidMount = function componentDidMount () {
    var this$1 = this;

    fetch('_ah/api/myApi/v1/scores')
      .then(function (response) { return response.json(); })
      .then(function (data) { return this$1.setState({  data : data.items }); });
  };
  MyApp.prototype.render = function render () {
    return React.createElement( 'ul', null, this.state.data.map(function (e) { return React.createElement( 'li', null, " ", e.properties.name, " ", e.properties.score, " " ); }), "  " );
  };

  
  
  return MyApp;
}(React.Component));

ReactDOM.render(
React.createElement('div', null,
    React.createElement(MyApp)
  )
,
  document.getElementById('app')
);

