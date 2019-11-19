# ####################################################################

#  Created by Genus(TM) Synthesis Solution 17.23-s026_1 on Tue Nov 19 04:48:39 UTC 2019

# ####################################################################

set sdc_version 2.0

set_units -capacitance 1.0fF
set_units -time 1.0ps

# Set the current design
current_design GCD

set_load -pin_load 1.0 [get_ports {z[31]}]
set_load -pin_load 1.0 [get_ports {z[30]}]
set_load -pin_load 1.0 [get_ports {z[29]}]
set_load -pin_load 1.0 [get_ports {z[28]}]
set_load -pin_load 1.0 [get_ports {z[27]}]
set_load -pin_load 1.0 [get_ports {z[26]}]
set_load -pin_load 1.0 [get_ports {z[25]}]
set_load -pin_load 1.0 [get_ports {z[24]}]
set_load -pin_load 1.0 [get_ports {z[23]}]
set_load -pin_load 1.0 [get_ports {z[22]}]
set_load -pin_load 1.0 [get_ports {z[21]}]
set_load -pin_load 1.0 [get_ports {z[20]}]
set_load -pin_load 1.0 [get_ports {z[19]}]
set_load -pin_load 1.0 [get_ports {z[18]}]
set_load -pin_load 1.0 [get_ports {z[17]}]
set_load -pin_load 1.0 [get_ports {z[16]}]
set_load -pin_load 1.0 [get_ports {z[15]}]
set_load -pin_load 1.0 [get_ports {z[14]}]
set_load -pin_load 1.0 [get_ports {z[13]}]
set_load -pin_load 1.0 [get_ports {z[12]}]
set_load -pin_load 1.0 [get_ports {z[11]}]
set_load -pin_load 1.0 [get_ports {z[10]}]
set_load -pin_load 1.0 [get_ports {z[9]}]
set_load -pin_load 1.0 [get_ports {z[8]}]
set_load -pin_load 1.0 [get_ports {z[7]}]
set_load -pin_load 1.0 [get_ports {z[6]}]
set_load -pin_load 1.0 [get_ports {z[5]}]
set_load -pin_load 1.0 [get_ports {z[4]}]
set_load -pin_load 1.0 [get_ports {z[3]}]
set_load -pin_load 1.0 [get_ports {z[2]}]
set_load -pin_load 1.0 [get_ports {z[1]}]
set_load -pin_load 1.0 [get_ports {z[0]}]
set_load -pin_load 1.0 [get_ports v]
group_path -weight 1.000000 -name cg_enable_group_default -through [list \
  [get_pins CLKGATE_RC_CG_HIER_INST0/enable]  \
  [get_pins CLKGATE_RC_CG_HIER_INST1/enable]  \
  [get_pins CLKGATE_RC_CG_HIER_INST0/enable]  \
  [get_pins CLKGATE_RC_CG_HIER_INST1/enable] ]
set_clock_gating_check -setup 0.0 
