IannisPreset {
  var <name, 
  <data, 
  <isFactory;

  *new {arg data;
    ^super.new.init(data);
  }

  init {arg theData;
    data = theData;
    name = theData[\name];
    isFactory = theData[\isFactory];
  }

  loadExternalData {arg newData;
    data = newData;
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
    data[\values] = newValue;
  }

  values {
    ^data[\values];
  }

  map_ {arg newValue;
    data[\map] = newValue;
  }

  map {
    data[\map]??{data[\map] = ()}
    ^data[\map];
  }

  midi_ {arg newValue;
    data[\midi] = newValue;
  }

  midi {
    data[\midi]??{data[\midi] = ()};
    ^data[\midi];
  }

  midiBindings {
    this.midi[\cc]??{data[\midi][\cc] = ()};
    ^data[\midi][\cc];
  }

  // map related
  initMapForKeyIfNeeded {arg key;
    this.map[key]??{this.map[key] = ()};
  }

  setMapCode {arg key, code;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\code] = code;
  }

  getMapCode {arg key;
    var output = this.map[key]!?{this.map[key][\code]};
    ^output;
  }

  setMapXFade {arg key, value;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\xfade] = value;
  }

  getMapXFade {arg key;
    var output = this.map[key]!?{this.map[key][\xfade]};
    ^output;
  }

  setMapState {arg key, value;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\state] = value;
  }

  getMapState {arg key;
    var output = this.map[key]!?{this.map[key][\state]};
    ^output;
  }

  setMapMode {arg key, value;
    this.initMapForKeyIfNeeded(key);

    this.map[key][\mode] = value;
  }

  getMapMode {arg key;
    var output = this.map[key]!?{this.map[key][\mode]};
    ^output;
  }

  setMapUIValueForKey {arg mapKey, parameterKey, value;
    this.initMapForKeyIfNeeded(mapKey);
    this.map[mapKey][\ui]??{this.map[mapKey][\ui] = ()};
    this.map[mapKey][\ui][parameterKey] = value;
  }

  getMapUIValues {arg key;
    var output = this.map[key]!?{this.map[key][\ui]};
    ^output;
  }

  // MIDI related

  addMIDIBinding {arg key, sourceUID, ccNum, channel;
    this.midiBindings[key] = [sourceUID, ccNum, channel];
  }

  removeMIDIBinding {arg key;
    this.midiBindings[key] = nil;
  }

  updateMIDIBinding {arg key, sourceUID, ccNum, channel;
    sourceUID!?{
      this.midiBindings[key][0] = sourceUID;
    };

    ccNum!?{
      this.midiBindings[key][1] = ccNum;
    };

    channel!?{
      this.midiBindings[key][2] = channel;
    };
  }

  getMIDISourceUIDOfBinding {arg key;
    ^this.midiBindings[key][0];
  }

  getMIDICCNumOfBinding {arg key;
    ^this.midiBindings[key][1];
  }

  getMIDIChannelOfBinding {arg key;
    ^this.midiBindings[key][2];
  }
}
