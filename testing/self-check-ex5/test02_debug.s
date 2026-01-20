.data
msg_div_zero: .asciiz "Illegal Division By Zero"
msg_null_ptr: .asciiz "Invalid Pointer Dereference"
msg_bounds: .asciiz "Access Violation"
i_10: .word 0
j_11: .word 0
min_12: .word 0
size_9: .word 0
arr_8: .word 0
minIndex_13: .word 0
arr_9: .word 0
p_10: .word 0
temp_14: .word 0

.text
.globl main

main:
    j __user_main    # Jump to main function
BubbleSort:

    # Allocate size_9
    # Allocate size_9

    # Allocate arr_8
    # Allocate arr_8

    # Allocate i_10
    # Allocate i_10

    # Temp_0 := 0
    li $t4, 0

    # i_10 := Temp_0
    sw $t4, i_10

    # Allocate j_11
    # Allocate j_11

    # Temp_1 := 0
    li $t4, 0

    # j_11 := Temp_1
    sw $t4, j_11

    # Allocate min_12
    # Allocate min_12

    # Temp_2 := 32767
    li $t4, 32767

    # min_12 := Temp_2
    sw $t4, min_12

    # Allocate minIndex_13
    # Allocate minIndex_13

    # Temp_3 := 1
    li $t4, 1

    # Temp_4 := -Temp_3
    # Saturated negation
    li $t9, -32768
    beq $t4, $t9, saturate_neg_0
    neg $t0, $t4
    j saturate_neg_done_1
saturate_neg_0:
    li $t0, 32767
saturate_neg_done_1:

    # minIndex_13 := Temp_4
    sw $t0, minIndex_13

    # Allocate temp_14
    # Allocate temp_14

    # Temp_5 := 0
    li $t0, 0

    # temp_14 := Temp_5
    sw $t0, temp_14

Label_1_start:

    # Temp_7 := i_10
    lw $t0, i_10

    # Temp_8 := size_9
    lw $t4, size_9

    # Temp_6 := Temp_7 < Temp_8
    slt $t0, $t0, $t4

    # JumpIfEqToZero Temp_6 Label_0_end
    beq $t0, $zero, Label_0_end

    # Temp_9 := i_10
    lw $t0, i_10

    # j_11 := Temp_9
    sw $t0, j_11

    # Temp_10 := 32767
    li $t0, 32767

    # min_12 := Temp_10
    sw $t0, min_12

Label_3_start:

    # Temp_12 := j_11
    lw $t0, j_11

    # Temp_13 := size_9
    lw $t4, size_9

    # Temp_11 := Temp_12 < Temp_13
    slt $t0, $t0, $t4

    # JumpIfEqToZero Temp_11 Label_2_end
    beq $t0, $zero, Label_2_end

    # Temp_15 := arr_8
    lw $t4, arr_8

    # Temp_16 := j_11
    lw $t0, j_11

    # Temp_17 := ARRAY_ACCESS(Temp_15[Temp_16], elemSize=4)
    # Check array bounds
    beq $t4, $zero, error_null_pointer
    bltz $t0, error_bounds
    lw $s0, 0($t4)    # load array length
    bge $t0, $s0, error_bounds
    sll $s0, $t0, 2    # index * 4
    addi $s0, $s0, 4    # + 4 (skip length)
    add $s0, $t4, $s0
    lw $t4, 0($s0)

    # Temp_18 := min_12
    lw $t0, min_12

    # Temp_14 := Temp_17 < Temp_18
    slt $t0, $t4, $t0

    # JumpIfEqToZero Temp_14 Label_5_end
    beq $t0, $zero, Label_5_end

    # Temp_19 := arr_8
    lw $t0, arr_8

    # Temp_20 := j_11
    lw $t4, j_11

    # Temp_21 := ARRAY_ACCESS(Temp_19[Temp_20], elemSize=4)
    # Check array bounds
    beq $t0, $zero, error_null_pointer
    bltz $t4, error_bounds
    lw $s0, 0($t0)    # load array length
    bge $t4, $s0, error_bounds
    sll $s0, $t4, 2    # index * 4
    addi $s0, $s0, 4    # + 4 (skip length)
    add $s0, $t0, $s0
    lw $t0, 0($s0)

    # min_12 := Temp_21
    sw $t0, min_12

    # Temp_22 := j_11
    lw $t0, j_11

    # minIndex_13 := Temp_22
    sw $t0, minIndex_13

Label_5_end:

    # Temp_24 := j_11
    lw $t4, j_11

    # Temp_25 := 1
    li $t0, 1

    # Temp_23 := Temp_24 + Temp_25
    # Saturated addition
    add $t0, $t4, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_2
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_3
    j saturate_add_done_4
saturate_add_max_2:
    li $t0, 32767
    j saturate_add_done_4
saturate_add_min_3:
    li $t0, -32768
saturate_add_done_4:

    # j_11 := Temp_23
    sw $t0, j_11

    # Jump Label_3_start
    j Label_3_start

Label_2_end:

    # Temp_26 := arr_8
    lw $t0, arr_8

    # Temp_27 := i_10
    lw $t4, i_10

    # Temp_28 := ARRAY_ACCESS(Temp_26[Temp_27], elemSize=4)
    # Check array bounds
    beq $t0, $zero, error_null_pointer
    bltz $t4, error_bounds
    lw $s0, 0($t0)    # load array length
    bge $t4, $s0, error_bounds
    sll $s0, $t4, 2    # index * 4
    addi $s0, $s0, 4    # + 4 (skip length)
    add $s0, $t0, $s0
    lw $t0, 0($s0)

    # temp_14 := Temp_28
    sw $t0, temp_14

    # Temp_29 := min_12
    lw $t5, min_12

    # Temp_30 := arr_8
    lw $t4, arr_8

    # Temp_31 := i_10
    lw $t0, i_10

    # ARRAY_STORE(Temp_30[Temp_31], Temp_29, elemSize=4)
    # Check array bounds
    beq $t4, $zero, error_null_pointer
    bltz $t0, error_bounds
    lw $s0, 0($t4)    # load array length
    bge $t0, $s0, error_bounds
    sll $s0, $t0, 2
    addi $s0, $s0, 4
    add $s0, $t4, $s0
    sw $t5, 0($s0)

    # Temp_32 := temp_14
    lw $t5, temp_14

    # Temp_33 := arr_8
    lw $t4, arr_8

    # Temp_34 := minIndex_13
    lw $t0, minIndex_13

    # ARRAY_STORE(Temp_33[Temp_34], Temp_32, elemSize=4)
    # Check array bounds
    beq $t4, $zero, error_null_pointer
    bltz $t0, error_bounds
    lw $s0, 0($t4)    # load array length
    bge $t0, $s0, error_bounds
    sll $s0, $t0, 2
    addi $s0, $s0, 4
    add $s0, $t4, $s0
    sw $t5, 0($s0)

    # Temp_36 := i_10
    lw $t0, i_10

    # Temp_37 := 1
    li $t4, 1

    # Temp_35 := Temp_36 + Temp_37
    # Saturated addition
    add $t0, $t0, $t4
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_5
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_6
    j saturate_add_done_7
saturate_add_max_5:
    li $t0, 32767
    j saturate_add_done_7
saturate_add_min_6:
    li $t0, -32768
saturate_add_done_7:

    # i_10 := Temp_35
    sw $t0, i_10

    # Jump Label_1_start
    j Label_1_start

Label_0_end:

__user_main:

    # Allocate arr_9
    # Allocate arr_9

    # arr_9 := (null)
    sw $zero, arr_9

    # Temp_38 := 34
    li $t4, 34

    # Temp_39 := arr_9
    lw $t0, arr_9

    # Temp_40 := 0
    li $t5, 0

    # ARRAY_STORE(Temp_39[Temp_40], Temp_38, elemSize=4)
    # Check array bounds
    beq $t0, $zero, error_null_pointer
    bltz $t5, error_bounds
    lw $s0, 0($t0)    # load array length
    bge $t5, $s0, error_bounds
    sll $s0, $t5, 2
    addi $s0, $s0, 4
    add $s0, $t0, $s0
    sw $t4, 0($s0)

    # Temp_41 := 12
    li $t0, 12

    # Temp_42 := arr_9
    lw $t5, arr_9

    # Temp_43 := 1
    li $t4, 1

    # ARRAY_STORE(Temp_42[Temp_43], Temp_41, elemSize=4)
    # Check array bounds
    beq $t5, $zero, error_null_pointer
    bltz $t4, error_bounds
    lw $s0, 0($t5)    # load array length
    bge $t4, $s0, error_bounds
    sll $s0, $t4, 2
    addi $s0, $s0, 4
    add $s0, $t5, $s0
    sw $t0, 0($s0)

    # Temp_44 := 600
    li $t0, 600

    # Temp_45 := -Temp_44
    # Saturated negation
    li $t9, -32768
    beq $t0, $t9, saturate_neg_8
    neg $t2, $t0
    j saturate_neg_done_9
saturate_neg_8:
    li $t2, 32767
saturate_neg_done_9:

    # Temp_46 := arr_9
    lw $t4, arr_9

    # Temp_47 := 2
    li $t0, 2

    # ARRAY_STORE(Temp_46[Temp_47], Temp_45, elemSize=4)
    # Check array bounds
    beq $t4, $zero, error_null_pointer
    bltz $t0, error_bounds
    lw $s0, 0($t4)    # load array length
    bge $t0, $s0, error_bounds
    sll $s0, $t0, 2
    addi $s0, $s0, 4
    add $s0, $t4, $s0
    sw $t2, 0($s0)

    # Temp_48 := 400
    li $t0, 400

    # Temp_49 := -Temp_48
    # Saturated negation
    li $t9, -32768
    beq $t0, $t9, saturate_neg_10
    neg $t3, $t0
    j saturate_neg_done_11
saturate_neg_10:
    li $t3, 32767
saturate_neg_done_11:

    # Temp_50 := arr_9
    lw $t0, arr_9

    # Temp_51 := 3
    li $t2, 3

    # ARRAY_STORE(Temp_50[Temp_51], Temp_49, elemSize=4)
    # Check array bounds
    beq $t0, $zero, error_null_pointer
    bltz $t2, error_bounds
    lw $s0, 0($t0)    # load array length
    bge $t2, $s0, error_bounds
    sll $s0, $t2, 2
    addi $s0, $s0, 4
    add $s0, $t0, $s0
    sw $t3, 0($s0)

    # Temp_52 := 70
    li $t2, 70

    # Temp_53 := arr_9
    lw $t0, arr_9

    # Temp_54 := 4
    li $t3, 4

    # ARRAY_STORE(Temp_53[Temp_54], Temp_52, elemSize=4)
    # Check array bounds
    beq $t0, $zero, error_null_pointer
    bltz $t3, error_bounds
    lw $s0, 0($t0)    # load array length
    bge $t3, $s0, error_bounds
    sll $s0, $t3, 2
    addi $s0, $s0, 4
    add $s0, $t0, $s0
    sw $t2, 0($s0)

    # Temp_55 := 30
    li $t3, 30

    # Temp_56 := arr_9
    lw $t0, arr_9

    # Temp_57 := 5
    li $t2, 5

    # ARRAY_STORE(Temp_56[Temp_57], Temp_55, elemSize=4)
    # Check array bounds
    beq $t0, $zero, error_null_pointer
    bltz $t2, error_bounds
    lw $s0, 0($t0)    # load array length
    bge $t2, $s0, error_bounds
    sll $s0, $t2, 2
    addi $s0, $s0, 4
    add $s0, $t0, $s0
    sw $t3, 0($s0)

    # Temp_58 := 580
    li $t0, 580

    # Temp_59 := -Temp_58
    # Saturated negation
    li $t9, -32768
    beq $t0, $t9, saturate_neg_12
    neg $t1, $t0
    j saturate_neg_done_13
saturate_neg_12:
    li $t1, 32767
saturate_neg_done_13:

    # Temp_60 := arr_9
    lw $t2, arr_9

    # Temp_61 := 6
    li $t0, 6

    # ARRAY_STORE(Temp_60[Temp_61], Temp_59, elemSize=4)
    # Check array bounds
    beq $t2, $zero, error_null_pointer
    bltz $t0, error_bounds
    lw $s0, 0($t2)    # load array length
    bge $t0, $s0, error_bounds
    sll $s0, $t0, 2
    addi $s0, $s0, 4
    add $s0, $t2, $s0
    sw $t1, 0($s0)

    # Temp_62 := 7
    li $t0, 7

    # size_9 := Temp_62
    sw $t0, size_9

    # Temp_63 := arr_9
    lw $t0, arr_9

    # arr_8 := Temp_63
    sw $t0, arr_8

    # Temp_64 := BubbleSort()
    jal BubbleSort
    move $t0, $v0

    # Allocate i_10
    # Allocate i_10

    # Temp_65 := 0
    li $t0, 0

    # i_10 := Temp_65
    sw $t0, i_10

Label_7_start:

    # Temp_67 := i_10
    lw $t1, i_10

    # Temp_68 := 7
    li $t0, 7

    # Temp_66 := Temp_67 < Temp_68
    slt $t0, $t1, $t0

    # JumpIfEqToZero Temp_66 Label_6_end
    beq $t0, $zero, Label_6_end

    # Temp_69 := arr_9
    lw $t0, arr_9

    # Temp_70 := i_10
    lw $t1, i_10

    # Temp_71 := ARRAY_ACCESS(Temp_69[Temp_70], elemSize=4)
    # Check array bounds
    beq $t0, $zero, error_null_pointer
    bltz $t1, error_bounds
    lw $s0, 0($t0)    # load array length
    bge $t1, $s0, error_bounds
    sll $s0, $t1, 2    # index * 4
    addi $s0, $s0, 4    # + 4 (skip length)
    add $s0, $t0, $s0
    lw $t0, 0($s0)

    # p_10 := Temp_71
    sw $t0, p_10

    # Temp_72 := PrintInt()
    jal PrintInt
    move $t0, $v0

    # Temp_74 := i_10
    lw $t1, i_10

    # Temp_75 := 1
    li $t0, 1

    # Temp_73 := Temp_74 + Temp_75
    # Saturated addition
    add $t0, $t1, $t0
    li $t9, 32767
    bgt $t0, $t9, saturate_add_max_14
    li $t9, -32768
    blt $t0, $t9, saturate_add_min_15
    j saturate_add_done_16
saturate_add_max_14:
    li $t0, 32767
    j saturate_add_done_16
saturate_add_min_15:
    li $t0, -32768
saturate_add_done_16:

    # i_10 := Temp_73
    sw $t0, i_10

    # Jump Label_7_start
    j Label_7_start

Label_6_end:

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

PrintString:
    # Print string (expects value in p_10 variable)
    lw $a0, p_10    # load string address to print
    li $v0, 4    # syscall: print_string
    syscall
    jr $ra    # return

# Program exit
    li $v0, 10
    syscall
