(
// For development only
// ----------------------------------------------------------
Quarks.install("~/Desktop/Iannis".standardizePath);
Quarks.uninstall("Iannis");
// ----------------------------------------------------------
)

s.boot;
SynthDef.synthDefDir = "~/Desktop/Iannis/synths".standardizePath;

TempoClock.default.tempo = 107/60;
(
b = Buffer.read(s, "~/Music/SuperCollider/livebox/Samples/02_Elemental/013SineAmplitude.wav".standardizePath);
c = Buffer.read(s, "~/Documents/Sounds/Drum loops/drumloop004BPM110(44,1kHz).wav".standardizePath);
d = Buffer.read(s, "~/Documents/Sounds/Drum loops/drumloop001BPM137(48kHz).aif".standardizePath);

e = Buffer.alloc(Server.default, 8192, 1);
f = Signal.rectWindow(8192);
e.loadCollection(f);
)

(
s.loadDirectory(SynthDef.synthDefDir);
SynthDescLib(\iannis_synth);
// init synthdescs for iannis' synthdefs
SynthDescLib.getLib(\iannis_synth).read(SynthDef.synthDefDir+/+"granulator.scsyndef");

// load iannis synthdesc lib into global lib
SynthDescLib.getLib(\iannis_synth).synthDescs.do({|item| SynthDescLib.global.add(item)});
)

IannisProbabilisticSequencerView(\sequencer, \granulator, patternLength: 4).front;

r = IannisRecorder("~/Desktop".standardizePath);
r.quant = 4;
r.record();
r.stopRecording();
r.playSample(true);
r.stopSample();

Server.freeAll;
