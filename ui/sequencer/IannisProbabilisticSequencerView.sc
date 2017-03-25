IannisProbabilisticSequencerView : CompositeView {
  var <parametersContainerView, 
  <parametersView,
  eventControllers,
  <userParameters,
  <availableParameters,
  parametersListView,
  addParameterButton,
	<sequencer;

	*new {arg name, instrument, numberOfPitches = 4, numberOfRhythmicFigures = 2, patternLength = 8;
		^super.new.init(name, instrument, numberOfPitches, numberOfRhythmicFigures, patternLength);
	}

	init {arg name, instrument, numberOfPitches, numberOfRhythmicFigures, patternLength;
    var evenStepsViewBackground = Color.gray(0.79, 1), 
    oddStepsViewBackground = Color.gray(0.72, 1), 
    evenParametersViewBackground = Color.gray(0.925, 1), 
    oddParametersViewBackground = Color.gray(0.85, 1);

    // sequencer
		sequencer = IannisProbabilisticSequencer(name, instrument, patternLength);

    // parameters container
    parametersContainerView = ScrollView.new;

    parametersContainerView.canvas = CompositeView.new;

    parametersContainerView.hasHorizontalScroller = false;
    parametersContainerView.hasVerticalScroller = true;
    parametersContainerView.hasBorder = false;

    parametersContainerView.minHeight = 200;

    //
    // init parameters
    //
        parametersListView = ListView.new;
    eventControllers = IdentityDictionary.new;
    availableParameters = ["pan", "legato", "sustain", "oct", "stretch", "tempo", "strum", "detune", "mtranspose", "gtranspose", "ctranspose"];
    // add synth parameters
    ~paramsWithoutDefaults = SynthDescLib.global.at(instrument.asSymbol).controlNames
    .select({arg item;
      (item != 'freq')
      .and(item != 'midinote')
      .and(item != 'note')
      .and(item != 'gate')
      .and(item != 'amp')
      .and(item != 'dur')
      .and(availableParameters.indexOfEqual(item.asString).isNil);
    });

    availableParameters = availableParameters++~paramsWithoutDefaults;
    userParameters = [];

    // pitch
    eventControllers[\degree] = IannisProbabilisticSequencerEventController(sequencer, "Note", \degree, numberOfPitches, true, this);
    eventControllers[\degree].stepsView.canvas.background = evenStepsViewBackground;
    eventControllers[\degree].parametersView.background = evenParametersViewBackground;
    // duration
    eventControllers[\dur] = IannisProbabilisticSequencerEventController(sequencer, "Rhythm", \dur, numberOfRhythmicFigures, true, this);
    eventControllers[\dur].stepsView.canvas.background = oddStepsViewBackground;
    eventControllers[\dur].parametersView.background = oddParametersViewBackground;
    // amp
    eventControllers[\amp] = IannisProbabilisticSequencerEventController(sequencer, "Amplitude", \amp, 1, true, this);
    eventControllers[\amp].stepsView.canvas.background = evenStepsViewBackground;
    eventControllers[\amp].parametersView.background = evenParametersViewBackground;

    // add parameters to container
    parametersContainerView.canvas.layout = VLayout(*eventControllers.values);
    parametersContainerView.canvas.layout.spacing = 0;
    parametersContainerView.canvas.layout.margins = 0!4;

    // add parameter button
    addParameterButton = Button.new;
    addParameterButton.fixedWidth = 200;
    addParameterButton.states = [["Add Parameters"]];
    addParameterButton.action = {arg button;
      if (button.value == 0) {this.showParameterChooser()};
    };

    // parameters view
		parametersView = IannisProbabilisticSequencerParametersView.new(sequencer);

		//
		// Layout
		//
    this.layout = VLayout(
      HLayout(nil, addParameterButton),
      parametersContainerView,
			parametersView
		);

		this.layout.spacing = 0;
    this.layout.margins = 0!4;
	}

  showParameterChooser {
    var screenCenter = Rect().centerIn(Window.screenBounds);
    var window = Window("Parameters", Rect(screenCenter.x-100, screenCenter.y-150, 200, 300), false);
    var addButton = Button.new;
    parametersListView = ListView.new;

    addParameterButton.enabled = false;

    parametersListView.items = availableParameters;
    parametersListView.selectionMode = \extended;
    parametersListView.action = {arg view;
      if (view.selection.size == 0) {
        addButton.enabled = false;
      };
      addButton.enabled = true;
    };


    // init add button with false state, because
    // on initialization there are no selections
    addButton.fixedWidth = 100;
    addButton.states = [["Add"]];

    addButton.action = {arg button;
      parametersListView.selection.do({arg index;
        var key = availableParameters.removeAt(index).asSymbol;
        var parameterController = IannisProbabilisticSequencerEventController(sequencer, key.asString, key, 1, false, this);
        parameterController.stepsView.canvas.background = Color.gray(0.72, 1);
        parameterController.parametersView.background = Color.gray(0.85, 1);

        parametersContainerView.canvas.layout.add(parameterController);

        userParameters = userParameters.add(key);
      });

      // update view
      parametersListView.items = availableParameters;
    };

    window.layout = VLayout(parametersListView, HLayout(nil, addButton));
    window.front;
    window.onClose = {addParameterButton.enabled = true};
  }

  userParameterWillClose {arg key;
    userParameters.removeAt(userParameters.indexOfEqual(key.asSymbol));
    availableParameters = availableParameters.add(key);

    if (parametersListView.notNil) {
      parametersListView.items = availableParameters;
      parametersListView.doAction();
    };
  }
}
