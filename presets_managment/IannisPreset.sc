IannisPreset {
  var <name, <data, <values, <isFactory;

  *new {arg data;
    ^super.new.init(data);
  }

  init {arg theData;
    data = theData;
    values = theData[\values];
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
}
