import sys
import re
import os
import tempfile
import shutil

"""
Remove conflicting specification statements found in PDK's DRC & LVS decks.
"""
extracted_tarballs_dir = sys.argv[1]
ruledirs = os.path.join(extracted_tarballs_dir, "ASAP7_PDKandLIB.tar/ASAP7_PDKandLIB_v1p5/asap7PDK_r1p5.tar.bz2/asap7PDK_r1p5/calibre/ruledirs")
drc_deck = os.path.join(ruledirs, "drc/drcRules_calibre_asap7_171111a.rul")
lvs_deck = os.path.join(ruledirs, "lvs/lvsRules_calibre_asap7_160819a.rul")
pattern = re.compile(".*(LAYOUT PATH|LAYOUT PRIMARY|LAYOUT SYSTEM|DRC RESULTS DATABASE|DRC SUMMARY REPORT|LVS REPORT|LVS POWER NAME|LVS GROUND NAME).*\n")
with tempfile.NamedTemporaryFile(delete=False) as tf:
    with open(drc_deck, 'r') as f:
        tf.write(pattern.sub("", f.read()).encode('utf-8'))
    shutil.copystat(drc_deck, tf.name)
    shutil.copy(tf.name, drc_deck)

with tempfile.NamedTemporaryFile(delete=False) as tf:
    with open(lvs_deck, 'r') as f:
        tf.write(pattern.sub("", f.read()).encode('utf-8'))
    shutil.copystat(lvs_deck, tf.name)
    shutil.copy(tf.name, lvs_deck)
