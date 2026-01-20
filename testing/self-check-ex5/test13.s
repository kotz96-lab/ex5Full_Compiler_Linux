.data
msg_div_zero: .asciiz "Illegal Division By Zero"
msg_null_ptr: .asciiz "Invalid Pointer Dereference"
msg_bounds: .asciiz "Access Violation"
i_7: .word 0
p_10: .word 0

.text
.globl main

main:
    j __user_main    # Jump to user's main function
foo:

    # Allocate i_7
    # Allocate i_7

    # Temp_0 := 174
    li $t0, 174

    # i_7 := Temp_0
    sw $t0, i_7

Label_1_start:

    # Temp_2 := i_7
    lw $t1, i_7

    # Temp_3 := 32764
    li $t0, 32764

    # Temp_1 := Temp_2 < Temp_3
    slt $t0, $t1, $t0

    # JumpIfEqToZero Temp_1 Label_0_end
    beq $t0, $zero, Label_0_end

    # Temp_5 := i_7
    lw $t0, i_7

    # Temp_6 := 55
    li $t1, 55

    # Temp_4 := Temp_5 + Temp_6
    # Saturated addition
    add $t0, $t0, $t1
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_0
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_1
    j saturate_add_done_2
saturate_add_max_0:
    li $t0, 32767
    j saturate_add_done_2
saturate_add_min_1:
    li $t0, -32768
saturate_add_done_2:

    # i_7 := Temp_4
    sw $t0, i_7

    # Jump Label_1_start
    j Label_1_start

Label_0_end:

    # Temp_8 := i_7
    lw $t1, i_7

    # Temp_9 := 666
    li $t0, 666

    # Temp_7 := Temp_8 + Temp_9
    # Saturated addition
    add $t0, $t1, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_3
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_4
    j saturate_add_done_5
saturate_add_max_3:
    li $t0, 32767
    j saturate_add_done_5
saturate_add_min_4:
    li $t0, -32768
saturate_add_done_5:

    # RETURN Temp_7
    move $v0, $t0    # return value
    jr $ra

__user_main:

    # Temp_12 := foo()
    jal foo
    move $t0, $v0

    # Temp_13 := foo()
    jal foo
    move $t1, $v0

    # Temp_11 := Temp_12 + Temp_13
    # Saturated addition
    add $t0, $t0, $t1
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

    # Temp_14 := foo()
    jal foo
    move $t1, $v0

    # Temp_10 := Temp_11 + Temp_14
    # Saturated addition
    add $t0, $t0, $t1
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

    # p_10 := Temp_10
    sw $t0, p_10

    # Temp_15 := PrintInt()
    jal PrintInt
    move $t0, $v0

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
