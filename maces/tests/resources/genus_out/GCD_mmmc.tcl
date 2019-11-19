#################################################################################
#
# Created by Genus(TM) Synthesis Solution 17.23-s026_1 on Tue Nov 19 04:48:40 UTC 2019
#
#################################################################################

## library_sets
create_library_set -name PVT_0P63V_100C.setup_set \
    -timing { RESOURCESDIR/asap7/asap7sc7p5t_24_AO_RVT_SS.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_INVBUF_RVT_SS.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_OA_RVT_SS.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_SEQ_RVT_SS.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_SIMPLE_RVT_SS.lib }
create_library_set -name PVT_0P77V_0C.hold_set \
    -timing { RESOURCESDIR/asap7/asap7sc7p5t_24_AO_RVT_FF.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_INVBUF_RVT_FF.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_OA_RVT_FF.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_SEQ_RVT_FF.lib \
              RESOURCESDIR/asap7/asap7sc7p5t_24_SIMPLE_RVT_FF.lib }

## timing_condition
create_timing_condition -name PVT_0P63V_100C.setup_cond \
    -library_sets { PVT_0P63V_100C.setup_set }
create_timing_condition -name PVT_0P77V_0C.hold_cond \
    -library_sets { PVT_0P77V_0C.hold_set }

## rc_corner
create_rc_corner -name PVT_0P63V_100C.setup_rc \
    -temperature 100.0 \
    -qrc_tech RESOURCESDIR/asap7/qrcTechFile_typ03_scaled4xV06 \
    -pre_route_res 1.0 \
    -pre_route_cap 1.0 \
    -pre_route_clock_res 0.0 \
    -pre_route_clock_cap 0.0 \
    -post_route_res {1.0 1.0 1.0} \
    -post_route_cap {1.0 1.0 1.0} \
    -post_route_cross_cap {1.0 1.0 1.0} \
    -post_route_clock_res {1.0 1.0 1.0} \
    -post_route_clock_cap {1.0 1.0 1.0}
create_rc_corner -name PVT_0P77V_0C.hold_rc \
    -temperature 0.0 \
    -qrc_tech RESOURCESDIR/asap7/qrcTechFile_typ03_scaled4xV06 \
    -pre_route_res 1.0 \
    -pre_route_cap 1.0 \
    -pre_route_clock_res 0.0 \
    -pre_route_clock_cap 0.0 \
    -post_route_res {1.0 1.0 1.0} \
    -post_route_cap {1.0 1.0 1.0} \
    -post_route_cross_cap {1.0 1.0 1.0} \
    -post_route_clock_res {1.0 1.0 1.0} \
    -post_route_clock_cap {1.0 1.0 1.0}

## delay_corner
create_delay_corner -name PVT_0P63V_100C.setup_delay \
    -early_timing_condition { PVT_0P63V_100C.setup_cond } \
    -late_timing_condition { PVT_0P63V_100C.setup_cond } \
    -early_rc_corner PVT_0P63V_100C.setup_rc \
    -late_rc_corner PVT_0P63V_100C.setup_rc
create_delay_corner -name PVT_0P77V_0C.hold_delay \
    -early_timing_condition { PVT_0P77V_0C.hold_cond } \
    -late_timing_condition { PVT_0P77V_0C.hold_cond } \
    -early_rc_corner PVT_0P77V_0C.hold_rc \
    -late_rc_corner PVT_0P77V_0C.hold_rc

## constraint_mode
create_constraint_mode -name my_constraint_mode \
    -sdc_files { RESOURCESDIR/genus_out/GCD_syn.sdc }

## analysis_view
create_analysis_view -name PVT_0P63V_100C.setup_view \
    -constraint_mode my_constraint_mode \
    -delay_corner PVT_0P63V_100C.setup_delay
create_analysis_view -name PVT_0P77V_0C.hold_view \
    -constraint_mode my_constraint_mode \
    -delay_corner PVT_0P77V_0C.hold_delay

## set_analysis_view
set_analysis_view -setup { PVT_0P63V_100C.setup_view } \
                  -hold { PVT_0P77V_0C.hold_view }
