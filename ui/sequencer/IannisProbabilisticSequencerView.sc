IannisProbabilisticSequencerView : CompositeView {
  var <parametersContainerView, 
  <pitchController, <rhythmController, <parametersView,
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

    parametersContainerView.canvas.layout = VLayout();
    parametersContainerView.canvas.layout.spacing = 0;
    parametersContainerView.canvas.layout.margins = 0!4;

    // init parameters
    // pitch controller
    pitchController = IannisProbabilisticSequencerEventController(sequencer, "Pitch", \midinote, numberOfPitches);
    pitchController.stepsView.canvas.background = evenStepsViewBackground;
		pitchController.parametersView.background = evenParametersViewBackground;
    
    // rhythm controller
    rhythmController = IannisProbabilisticSequencerEventController(sequencer, "Rhythm", \dur, numberOfRhythmicFigures);
    rhythmController.stepsView.canvas.background = oddStepsViewBackground;
    rhythmController.parametersView.background = oddParametersViewBackground;

    // add parameters to container
    parametersContainerView.canvas.layout.add(pitchController);
    parametersContainerView.canvas.layout.add(rhythmController);

    // add controllers for synth parameters
    SynthDescLib.global.at(instrument.asSymbol).controlNames.do({arg paramName, n;
      if((paramName != 'freq').and(paramName != 'midinote').and(paramName != 'gate'), {
        var background = [];
        var newParameterController = IannisProbabilisticSequencerEventController(sequencer, paramName, paramName.asSymbol, 4);

        // applying background
        if((n%2).even, {
          background = background.add(evenStepsViewBackground);
          background = background.add(evenParametersViewBackground);
        }, {
          background = background.add(oddStepsViewBackground);
          background = background.add(oddParametersViewBackground);
        });

        newParameterController.stepsView.canvas.background = background[0];
        newParameterController.parametersView.background = background[1];

        // add new parameter controller to layout
        parametersContainerView.canvas.layout.add(newParameterController);
      });
    });

    // parameters view
		parametersView = IannisProbabilisticSequencerParametersView.new(sequencer);


		//
		// Layout
		//
		this.layout = VLayout(
			parametersView,
      parametersContainerView
		);

		this.layout.spacing = 0;
    this.layout.margins = 0!4;
	}

}
