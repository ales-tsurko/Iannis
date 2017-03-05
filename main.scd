(
// For development only
// ----------------------------------------------------------
LanguageConfig.removeIncludePath(Document.current.dir);
LanguageConfig.addIncludePath(Document.current.dir);
LanguageConfig.store;
thisProcess.recompile;
// ----------------------------------------------------------
)


w = Window.new;
a = IannisProbabilisticSequencerView(\sequencer, 7);
w.layout = VLayout(a);

w.front;

TempoClock.default.tempo = 107/60;

p = IannisProbabilisticSequencer(\seq, \default, 8);
// изменения здесь не всегда заметны сразу,
// но здесь не должно быть regenerate, именно
// это помогает создавать совсем небольшие изменения
// в паттерне, а не менять паттерн целиком
p.updatePitches([0,2,4,9,11]+48, [1,1,10,10,2]);
p.play;
p.regenerate;
p.changeLength(4);

r = Dictionary.new;
r[\quarter] = 0;
r[\sixteenth] = 1;
r[\dotedEight] = 0.1;
r[\eight] = 0;
p.updateRhythm(r);

p.reset;
p.stop;

(
//
// GUI
//
var window, knobA, patFunc, previousValue;
window = Window.new;
knobA = Knob.new(window);
knobA.mode = \vert;

patFunc = {arg seqName, prob, seqLength;
	var pat = Pbind(
		\midinote, Pwrand(
			// здесь должно быть определенное количество ступеней, как
			// в обычном секвенсере и будет определяться, какие на них ноты
			// но этим ступеням будет соответствовать вероятность
			// применяться она должна после того, как поднимиться кнопка мыши
			// то есть не при каждом изменении, тогда получится круто
			// вероятность может быть равной 0

			// в ритме вероятность ритмических фигур определяется

			// также нужен параметр — транспозиция

			[48, 60, 64, 67, 71],
			[prob, 0.5,  0.5, 0.5,  0.5].normalizeSum,
			inf),
		\dur, Pwrand([
			Pseq([0.25, 0.25]),
			Pseq([1.5, 0.5]),
			Pseq([1])
		], [4, 1, 1].normalizeSum, inf)
	);

	Pdef(seqName,
		// второе число можно сделать очень длинным, тогда будет иллюзия
		// постоянного рандома
		Pn(Pfindur(seqLength, Pseed(2147483647.rand, pat)), inf)
	);
};

previousValue = knobA.value;
knobA.mouseUpAction = {arg knob;
	if(previousValue != knob.value, {
		patFunc.value(\sequencer, knob.value, 8);
	});
	previousValue = knob.value;
};

window.front;

Pdef('sequencer').play;
// Pdef('sequencer2').play;
)