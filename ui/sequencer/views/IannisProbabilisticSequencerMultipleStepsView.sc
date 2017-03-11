IannisProbabilisticSequencerMultipleStepsView : ScrollView {
	var <label, <numOfSteps, <parentController;

	*new {arg numberOfSteps, parentController;
		^super.new.init(numberOfSteps, parentController);
	}

	init {arg numberOfSteps, parentCtrlr;
    parentController = parentCtrlr;
    numOfSteps = numberOfSteps;

		label = StaticText.new;
		label.string = parentController.name;

		this.canvas = CompositeView();

		this.hasHorizontalScroller = false;
		this.hasVerticalScroller = true;
		this.hasBorder = false;
		this.autohidesScrollers = false;

		this.updateSteps(numberOfSteps);

		this.minHeight = 200;
		this.minWidth = 940;

		this.layout = VLayout(label, nil);

	}

	updateSteps {arg numberOfSteps;
    numOfSteps = numberOfSteps;
		this.canvas.layout = GridLayout();
		this.canvas.layout.vSpacing = 30;

		numberOfSteps.do({arg n;
			var ch = IannisProbabilisticSequencerStepView.new(n+1, n, this);

			ch.probabilitySlider.valueAction = parentController.data[\probability][n];
			ch.expressionField.valueAction = parentController.data[\expression][n];

			this.canvas.layout.add(ch, floor(n/4), n%4)
		});

    parentController.updatePattern();
	}

	updateParentData {
		canvas.children.do({arg item;
			if(item.isKindOf(IannisProbabilisticSequencerStepView), {
				var n = item.number;
				parentController.data[\probability][n] = item.probabilitySlider.value;
				parentController.data[\expression][n] = item.expressionField.string;
				parentController.data[\realExpression][n] = item.realExpression;
			});
		});
	}
}
