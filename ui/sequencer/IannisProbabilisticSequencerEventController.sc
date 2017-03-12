IannisProbabilisticSequencerEventController : CompositeView {
  var <data, <stepsView, <parametersView, <eventKey, <name, <sequencer, <>numberOfSteps;

  *new {arg sequencer, name, eventKey, numberOfSteps = 4;
    ^super.new.init(sequencer, name, eventKey, numberOfSteps);
  }

  init {arg seq, argName, event, numOfSteps;
    numberOfSteps = numOfSteps;
    sequencer = seq;
    name = argName;
    eventKey = event;

		data = Dictionary.new;
		data[\expression] = [];
		data[\realExpression] = [];
		data[\probability] = [];
    data[\transposition] = 0;
    data[\mul] = 1;
		128.do({arg n;
			data[\expression] = data[\expression].add("");
			data[\realExpression] = data[\realExpression].add(nil);
			data[\probability] = data[\probability].add(0);
		});

		stepsView = IannisProbabilisticSequencerMultipleStepsView.new(numberOfSteps, this);
		parametersView = IannisProbabilisticSequencerStepsParametersView.new(numberOfSteps, this);

		//
		// Layout
		//
		this.layout = HLayout(
      stepsView, parametersView
		);

		this.layout.spacing = 0;
    this.layout.margins = 0!4;
  }

  stepAction {arg stepView;
    var n = stepView.number;

    data[\probability][n] = stepView.probabilitySlider.value;
    data[\expression][n] = stepView.expressionField.string;
    data[\realExpression][n] = stepView.realExpression;

    this.updatePattern();
  }

  updatePattern {
    // update number of steps in sequencer
    sequencer.length = numberOfSteps;

    // updatePattern
    sequencer.updateEvent(eventKey, data[\realExpression], data[\probability], data[\mul], data[\transposition]);
  }
}
