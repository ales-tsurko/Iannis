~innerSynthDefsPath = "~/Desktop/Iannis/inner_synthdefs".standardizePath;

// track controller
SynthDef("by.alestsurko.iannis.track.controller", {arg 
  gain = 0, 
  pan = 0, 
  inbus = 0, 
  outbus = 0,
  levelbus;
  var in, out;
  in = In.ar(inbus, 2);
  out = Balance2.ar(in[0], in[1], pan, gain.dbamp);

  // level output
  Out.kr(levelbus, [
    out[0],
    out[1],
    Peak.ar(out[0], Impulse.kr(1/7)), // left peak
    Peak.ar(out[1], Impulse.kr(1/7))  // right peak
  ]);

  // main output
  ReplaceOut.ar(outbus, out);
}).writeDefFile(~innerSynthDefsPath);
