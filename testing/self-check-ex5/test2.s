.data
msg_div_zero: .asciiz "Illegal Division By Zero"
msg_null_ptr: .asciiz "Invalid Pointer Dereference"
msg_bounds: .asciiz "Access Violation"
p_10: .word 0

.text
.globl main

main:
    j __user_main    # Jump to user's main function
    # main:
__user_main:

    # Temp_1 := 1
    li $t0, 1

    # Temp_3 := 2
    li $t1, 2

    # Temp_4 := 3
    li $t2, 3

    # Temp_2 := Temp_3 * Temp_4
    # Saturated multiplication
    mul $t1, $t1, $t2
    li $t9, 32767
    bgt $t1, $t9, saturate_mul_max_0
    li $t9, -32768
    blt $t1, $t9, saturate_mul_min_1
    j saturate_mul_done_2
saturate_mul_max_0:
    li $t1, 32767
    j saturate_mul_done_2
saturate_mul_min_1:
    li $t1, -32768
saturate_mul_done_2:

    # Temp_0 := Temp_1 + Temp_2
    # Saturated addition
    add $t0, $t0, $t1
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

    # p_10 := Temp_0
    sw $t0, p_10

    # Temp_5 := PrintInt()
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
