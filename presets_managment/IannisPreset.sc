IannisPreset {
  var <name, <data, <values, <map, <isFactory;

  *new {arg data;
    ^super.new.init(data);
  }

  init {arg theData;
    data = theData;
    values = theData[\values];
    map = theData[\map]?();
    name = theData[\name];
    isFactory = theData[\isFactory];
  }

  name_ {arg newValue;
    name = newValue;
    data[\name] = newValue;
  }

  isFactory_ {arg newValue;
    isFactory = newValue;
    data[\isFactory] = newValue;
  }

  values_ {arg newValue;
    values = newValue;
    data[\values] = newValue;
  }

  map_ {arg newValue;
    map = newValue;
    data[\map] = newValue;
  }

  initMapForKeyIfNeeded {arg key;
    this.map[key]??{this.map[key] = ()};
  }

  setMapCode {arg key, code;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\code] = code;

    data[\map] = map;
  }

  getMapCode {arg key;
    ^this.map[key][\code];
  }

  setMapXFade {arg key, value;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\xfade] = value;

    data[\map] = map;
  }

  getMapXFade {arg key;
    ^this.map[key][\xfade];
  }

  setMapState {arg key, value;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\state] = value;

    data[\map] = map;
  }

  getMapState {arg key;
    ^this.map[key][\state];
  }

  setMapMode {arg key, value;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\mode] = value;

    data[\map] = map;
  }

  getMapMode {arg key;
    ^this.map[key][\mode];
  }

  setMapUIValueForKey {arg mapKey, parameterKey, value;
    this.initMapForKeyIfNeeded(mapKey);
    this.map[mapKey][\ui]??{this.map[mapKey][\ui] = ()};
    this.map[mapKey][\ui][parameterKey] = value;

    data[\map] = map;
  }

  getMapUIValues {arg key;
    ^this.map[key][\ui];
  }
}
