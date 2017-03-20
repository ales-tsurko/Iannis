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

  setAdd {arg field, duration = 1;
			var expr = field.value.interpret;
			// if there is no except spaces -- assign 0
			if(field.value.findRegexp("[^ \t]").size == 0, {
				field.value = 0;
				expr = 0;
			});

			if(expr.notNil, {
        sequencer.setAdd(eventKey, expr, duration);
			});
  }

  setMul {arg field, duration = 1;
			var expr = field.value.interpret;
			// if there is no anything except spaces -- assign 1
			if(field.value.findRegexp("[^ \t]").size == 0, {
				field.value = 1;
				expr = 1;
			});

			if(expr.notNil, {
        sequencer.setMul(eventKey, expr, duration);
			});
  }

  updatePattern {
    // updatePattern
    sequencer.updateEvent(eventKey, data[\realExpression], data[\probability], numberOfSteps);
  }
}
