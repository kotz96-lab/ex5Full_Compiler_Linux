.data
msg_div_zero: .asciiz "Illegal Division By Zero"
msg_null_ptr: .asciiz "Invalid Pointer Dereference"
msg_bounds: .asciiz "Access Violation"
joy_6: .word 0
alex_7: .word 0
happy_8: .word 0
p_10: .word 0
counter_5: .word 0
alex_11: .word 0

.text
.globl main

main:
    # Allocate counter_5
    # Allocate counter_5

    # Temp_0 := 7
    li $t0, 7

    # counter_5 := Temp_0
    sw $t0, counter_5

    # Allocate joy_6
    # Allocate joy_6

    # Temp_1 := 9
    li $t0, 9

    # joy_6 := Temp_1
    sw $t0, joy_6

    # Allocate alex_7
    # Allocate alex_7

    # Temp_2 := 10
    li $t0, 10

    # alex_7 := Temp_2
    sw $t0, alex_7

    # Allocate happy_8
    # Allocate happy_8

    # Temp_3 := 6
    li $t0, 6

    # happy_8 := Temp_3
    sw $t0, happy_8

    j __user_main    # Jump to main function
__user_main:

    # Allocate alex_11
    # Allocate alex_11

    # Temp_5 := 1
    li $t6, 1

    # Temp_7 := 2
    li $t1, 2

    # Temp_9 := 3
    li $t3, 3

    # Temp_11 := 4
    li $t0, 4

    # Temp_13 := 5
    li $t4, 5

    # Temp_15 := 6
    li $t5, 6

    # Temp_16 := 7
    li $t2, 7

    # Temp_14 := Temp_15 + Temp_16
    # Saturated addition
    add $t2, $t5, $t2
    li $t9, 32767
    bgt $t2, $t9, saturate_add_max_0
    li $t9, -32768
    blt $t2, $t9, saturate_add_min_1
    j saturate_add_done_2
saturate_add_max_0:
    li $t2, 32767
    j saturate_add_done_2
saturate_add_min_1:
    li $t2, -32768
saturate_add_done_2:

    # Temp_12 := Temp_13 + Temp_14
    # Saturated addition
    add $t2, $t4, $t2
    li $t9, 32767
    bgt $t2, $t9, saturate_add_max_3
    li $t9, -32768
    blt $t2, $t9, saturate_add_min_4
    j saturate_add_done_5
saturate_add_max_3:
    li $t2, 32767
    j saturate_add_done_5
saturate_add_min_4:
    li $t2, -32768
saturate_add_done_5:

    # Temp_10 := Temp_11 + Temp_12
    # Saturated addition
    add $t0, $t0, $t2
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_6
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_7
    j saturate_add_done_8
saturate_add_max_6:
    li $t0, 32767
    j saturate_add_done_8
saturate_add_min_7:
    li $t0, -32768
saturate_add_done_8:

    # Temp_8 := Temp_9 + Temp_10
    # Saturated addition
    add $t0, $t3, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_9
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_10
    j saturate_add_done_11
saturate_add_max_9:
    li $t0, 32767
    j saturate_add_done_11
saturate_add_min_10:
    li $t0, -32768
saturate_add_done_11:

    # Temp_6 := Temp_7 + Temp_8
    # Saturated addition
    add $t0, $t1, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_12
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_13
    j saturate_add_done_14
saturate_add_max_12:
    li $t0, 32767
    j saturate_add_done_14
saturate_add_min_13:
    li $t0, -32768
saturate_add_done_14:

    # Temp_4 := Temp_5 + Temp_6
    # Saturated addition
    add $t0, $t6, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_15
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_16
    j saturate_add_done_17
saturate_add_max_15:
    li $t0, 32767
    j saturate_add_done_17
saturate_add_min_16:
    li $t0, -32768
saturate_add_done_17:

    # alex_11 := Temp_4
    sw $t0, alex_11

    # Temp_18 := joy_6
    lw $t0, joy_6

    # Temp_19 := counter_5
    lw $t1, counter_5

    # Temp_17 := Temp_18 + Temp_19
    # Saturated addition
    add $t0, $t0, $t1
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_18
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_19
    j saturate_add_done_20
saturate_add_max_18:
    li $t0, 32767
    j saturate_add_done_20
saturate_add_min_19:
    li $t0, -32768
saturate_add_done_20:

    # happy_8 := Temp_17
    sw $t0, happy_8

    # Temp_21 := alex_11
    lw $t0, alex_11

    # Temp_22 := joy_6
    lw $t1, joy_6

    # Temp_20 := Temp_21 + Temp_22
    # Saturated addition
    add $t0, $t0, $t1
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_21
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_22
    j saturate_add_done_23
saturate_add_max_21:
    li $t0, 32767
    j saturate_add_done_23
saturate_add_min_22:
    li $t0, -32768
saturate_add_done_23:

    # counter_5 := Temp_20
    sw $t0, counter_5

    # Temp_24 := happy_8
    lw $t2, happy_8

    # Temp_26 := happy_8
    lw $t1, happy_8

    # Temp_27 := joy_6
    lw $t0, joy_6

    # Temp_25 := Temp_26 * Temp_27
    # Saturated multiplication
    mul $t0, $t1, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_mul_max_24
    li $t9, -32768
    blt $t0, $t9, saturate_mul_min_25
    j saturate_mul_done_26
saturate_mul_max_24:
    li $t0, 32767
    j saturate_mul_done_26
saturate_mul_min_25:
    li $t0, -32768
saturate_mul_done_26:

    # Temp_23 := Temp_24 + Temp_25
    # Saturated addition
    add $t0, $t2, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_27
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_28
    j saturate_add_done_29
saturate_add_max_27:
    li $t0, 32767
    j saturate_add_done_29
saturate_add_min_28:
    li $t0, -32768
saturate_add_done_29:

    # p_10 := Temp_23
    sw $t0, p_10

    # Temp_28 := PrintInt()
    jal PrintInt
    move $t0, $v0

    # Exit program
    li $v0, 10
    syscall

error_div_by_zero:
    la $a0, msg_div_zero
    li $v0, 4
    syscall
    li $v0, 10
    syscall

error_null_pointer:
    la $a0, msg_null_ptr
    li $v0, 4
    syscall
    li $v0, 10
    syscall

error_bounds:
    la $a0, msg_bounds
    li $v0, 4
    syscall
    li $v0, 10
    syscall

PrintInt:
    # Print integer (expects value in p_10 variable)
    lw $a0, p_10    # load value to print
    li $v0, 1    # syscall: print_int
    syscall
    li $a0, 32    # print space
    li $v0, 11    # syscall: print_char
    syscall
    jr $ra    # return

# Program exit
    li $v0, 10
    syscall
