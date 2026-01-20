.data
msg_div_zero: .asciiz "Illegal Division By Zero"
msg_null_ptr: .asciiz "Invalid Pointer Dereference"
msg_bounds: .asciiz "Access Violation"
a_7: .word 0
b_8: .word 0
p_10: .word 0

.text
.globl main

main:
    j __user_main    # Jump to user's main function
add:

    # Allocate b_8
    # Allocate b_8

    # Allocate a_7
    # Allocate a_7

    # Temp_1 := a_7
    lw $t1, a_7

    # Temp_2 := b_8
    lw $t0, b_8

    # Temp_0 := Temp_1 + Temp_2
    # Saturated addition
    add $t0, $t1, $t0
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

    # RETURN Temp_0
    move $v0, $t0    # return value
    jr $ra

__user_main:

    # Temp_3 := 7
    li $t0, 7

    # b_8 := Temp_3
    sw $t0, b_8

    # Temp_4 := 5
    li $t0, 5

    # a_7 := Temp_4
    sw $t0, a_7

    # Temp_5 := add()
    jal add
    move $t0, $v0

    # p_10 := Temp_5
    sw $t0, p_10

    # Temp_6 := PrintInt()
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
